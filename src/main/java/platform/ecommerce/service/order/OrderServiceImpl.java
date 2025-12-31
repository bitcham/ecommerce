package platform.ecommerce.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.domain.order.*;
import platform.ecommerce.dto.request.order.*;
import platform.ecommerce.dto.response.order.*;
import platform.ecommerce.exception.*;
import platform.ecommerce.repository.MemberRepository;
import platform.ecommerce.repository.order.OrderRepository;
import platform.ecommerce.security.SecurityUtils;
import platform.ecommerce.service.email.EmailService;
import platform.ecommerce.service.notification.NotificationService;
import platform.ecommerce.service.product.ProductService;

/**
 * Order service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public OrderResponse createOrder(Long memberId, OrderCreateRequest request) {
        log.info("Creating order for member: {}", memberId);

        ShippingAddress shippingAddress = ShippingAddress.builder()
                .recipientName(request.shippingAddress().recipientName())
                .recipientPhone(request.shippingAddress().recipientPhone())
                .zipCode(request.shippingAddress().zipCode())
                .address(request.shippingAddress().address())
                .addressDetail(request.shippingAddress().addressDetail())
                .build();

        Order order = Order.builder()
                .memberId(memberId)
                .shippingAddress(shippingAddress)
                .shippingFee(request.shippingFee())
                .discountAmount(request.discountAmount())
                .build();

        // First, add all items to the order (no side effects)
        for (OrderItemRequest itemRequest : request.items()) {
            order.addItem(
                    itemRequest.productId(),
                    itemRequest.productOptionId(),
                    itemRequest.productName(),
                    itemRequest.optionName(),
                    itemRequest.unitPrice(),
                    itemRequest.quantity()
            );
        }

        // Validate order before any stock operations
        order.validateForPlacement();

        // Only decrease stock after validation passes
        for (OrderItemRequest itemRequest : request.items()) {
            productService.decreaseStock(
                    itemRequest.productId(),
                    itemRequest.productOptionId(),
                    itemRequest.quantity()
            );
        }

        Order savedOrder = orderRepository.save(order);

        log.info("Order created: orderNumber={}", savedOrder.getOrderNumber());
        return toResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrder(Long orderId, Long memberId) {
        Order order = findOrderById(orderId);
        validateOrderOwnershipOrAdmin(order, memberId);
        return toResponse(order);
    }

    @Override
    public OrderResponse getOrderByNumber(String orderNumber, Long memberId) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        validateOrderOwnershipOrAdmin(order, memberId);
        return toResponse(order);
    }

    @Override
    public Page<OrderResponse> getMyOrders(Long memberId, Pageable pageable) {
        OrderSearchCondition condition = OrderSearchCondition.ofMember(memberId);
        return orderRepository.search(condition, pageable).map(this::toResponse);
    }

    @Override
    public Page<OrderResponse> searchOrders(OrderSearchCondition condition, Pageable pageable) {
        return orderRepository.search(condition, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse processPayment(Long orderId, PaymentMethod paymentMethod, String transactionId) {
        log.info("Processing payment for order: id={}, method={}", orderId, paymentMethod);

        Order order = findOrderById(orderId);
        order.markAsPaid(paymentMethod, transactionId);

        // Send order confirmation email and notification
        sendOrderConfirmationEmail(order);
        notificationService.notifyOrderStatusChange(order.getMemberId(), order.getOrderNumber(), "결제 완료");

        log.info("Payment processed for order: id={}", orderId);
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse startPreparing(Long orderId) {
        log.info("Starting preparation for order: id={}", orderId);

        Order order = findOrderById(orderId);
        order.startPreparing();

        log.info("Order preparation started: id={}", orderId);
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse shipOrder(Long orderId, String trackingNumber) {
        log.info("Shipping order: id={}, trackingNumber={}", orderId, trackingNumber);

        Order order = findOrderById(orderId);
        order.ship(trackingNumber);

        // Send order shipped email and notification
        sendOrderShippedEmail(order);
        notificationService.notifyDeliveryUpdate(order.getMemberId(), order.getOrderNumber(), trackingNumber);

        log.info("Order shipped: id={}", orderId);
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse deliverOrder(Long orderId) {
        log.info("Delivering order: id={}", orderId);

        Order order = findOrderById(orderId);
        order.deliver();

        log.info("Order delivered: id={}", orderId);
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long memberId, String reason) {
        log.info("Cancelling order: id={}, memberId={}, reason={}", orderId, memberId, reason);

        Order order = findOrderById(orderId);
        validateOrderOwnership(order, memberId);

        // Restore stock for all items before cancelling
        restoreStockForOrder(order);

        order.cancel(reason);

        log.info("Order cancelled: id={}", orderId);
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrderItem(Long orderId, Long memberId, Long orderItemId, String reason) {
        log.info("Cancelling order item: orderId={}, memberId={}, itemId={}, reason={}", orderId, memberId, orderItemId, reason);

        Order order = findOrderById(orderId);
        validateOrderOwnership(order, memberId);

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        // Restore stock for cancelled item
        productService.increaseStock(item.getProductId(), item.getProductOptionId(), item.getQuantity());
        item.cancel();

        log.info("Order item cancelled: orderId={}, itemId={}", orderId, orderItemId);
        return toResponse(order);
    }

    // ========== Private Helper Methods ==========

    private Order findOrderById(Long orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void validateOrderOwnership(Order order, Long memberId) {
        if (!order.getMemberId().equals(memberId)) {
            throw new InvalidStateException(ErrorCode.FORBIDDEN, "Not authorized to access this order");
        }
    }

    private void validateOrderOwnershipOrAdmin(Order order, Long memberId) {
        // Admin can access any order
        if (SecurityUtils.hasRole("ADMIN")) {
            return;
        }
        validateOrderOwnership(order, memberId);
    }

    private void sendOrderConfirmationEmail(Order order) {
        memberRepository.findById(order.getMemberId()).ifPresent(member ->
                emailService.sendOrderConfirmationEmail(
                        member.getEmail(),
                        member.getName(),
                        order.getOrderNumber()
                )
        );
    }

    private void sendOrderShippedEmail(Order order) {
        memberRepository.findById(order.getMemberId()).ifPresent(member ->
                emailService.sendOrderShippedEmail(
                        member.getEmail(),
                        member.getName(),
                        order.getOrderNumber(),
                        order.getTrackingNumber()
                )
        );
    }

    private void restoreStockForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getStatus() != OrderItemStatus.CANCELLED) {
                productService.increaseStock(item.getProductId(), item.getProductOptionId(), item.getQuantity());
            }
        }
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .memberId(order.getMemberId())
                .status(order.getStatus())
                .shippingAddress(toShippingAddressResponse(order.getShippingAddress()))
                .paymentMethod(order.getPaymentMethod())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream().map(this::toItemResponse).toList())
                .trackingNumber(order.getTrackingNumber())
                .paidAt(order.getPaidAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private ShippingAddressResponse toShippingAddressResponse(ShippingAddress address) {
        if (address == null) return null;
        return ShippingAddressResponse.builder()
                .recipientName(address.getRecipientName())
                .recipientPhone(address.getRecipientPhone())
                .zipCode(address.getZipCode())
                .address(address.getAddress())
                .addressDetail(address.getAddressDetail())
                .fullAddress(address.getFullAddress())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productOptionId(item.getProductOptionId())
                .productName(item.getProductName())
                .optionName(item.getOptionName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .status(item.getStatus())
                .build();
    }
}
