package platform.ecommerce.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.order.*;
import platform.ecommerce.dto.request.order.*;
import platform.ecommerce.exception.*;
import platform.ecommerce.repository.order.OrderRepository;
import platform.ecommerce.security.SecurityUtils;
import platform.ecommerce.service.product.ProductService;

/**
 * Domain service implementation for Order.
 * Contains pure business logic without DTO conversion or side effects.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Override
    @Transactional
    public Order createOrder(Long memberId, OrderCreateRequest request) {
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

        order.validateForPlacement();

        for (OrderItemRequest itemRequest : request.items()) {
            productService.decreaseStock(
                    itemRequest.productId(),
                    itemRequest.productOptionId(),
                    itemRequest.quantity()
            );
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created: orderNumber={}", savedOrder.getOrderNumber());
        return savedOrder;
    }

    @Override
    public Order getOrder(Long orderId) {
        return findOrderById(orderId);
    }

    @Override
    public Order getOrder(Long orderId, Long memberId) {
        Order order = findOrderById(orderId);
        validateOrderOwnershipOrAdmin(order, memberId);
        return order;
    }

    @Override
    public Order getOrderByNumber(String orderNumber, Long memberId) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        validateOrderOwnershipOrAdmin(order, memberId);
        return order;
    }

    @Override
    public Page<Order> getMyOrders(Long memberId, Pageable pageable) {
        OrderSearchCondition condition = OrderSearchCondition.ofMember(memberId);
        return orderRepository.search(condition, pageable);
    }

    @Override
    public Page<Order> searchOrders(OrderSearchCondition condition, Pageable pageable) {
        return orderRepository.search(condition, pageable);
    }

    @Override
    @Transactional
    public Order processPayment(Long orderId, PaymentMethod paymentMethod, String transactionId) {
        log.info("Processing payment for order: id={}, method={}", orderId, paymentMethod);

        Order order = findOrderById(orderId);
        order.markAsPaid(paymentMethod, transactionId);

        log.info("Payment processed for order: id={}", orderId);
        return order;
    }

    @Override
    @Transactional
    public Order startPreparing(Long orderId) {
        log.info("Starting preparation for order: id={}", orderId);

        Order order = findOrderById(orderId);
        order.startPreparing();

        log.info("Order preparation started: id={}", orderId);
        return order;
    }

    @Override
    @Transactional
    public Order shipOrder(Long orderId, String trackingNumber) {
        log.info("Shipping order: id={}, trackingNumber={}", orderId, trackingNumber);

        Order order = findOrderById(orderId);
        order.ship(trackingNumber);

        log.info("Order shipped: id={}", orderId);
        return order;
    }

    @Override
    @Transactional
    public Order deliverOrder(Long orderId) {
        log.info("Delivering order: id={}", orderId);

        Order order = findOrderById(orderId);
        order.deliver();

        log.info("Order delivered: id={}", orderId);
        return order;
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId, Long memberId, String reason) {
        log.info("Cancelling order: id={}, memberId={}, reason={}", orderId, memberId, reason);

        Order order = findOrderById(orderId);
        validateOrderOwnership(order, memberId);

        restoreStockForOrder(order);
        order.cancel(reason);

        log.info("Order cancelled: id={}", orderId);
        return order;
    }

    @Override
    @Transactional
    public Order cancelOrderItem(Long orderId, Long memberId, Long orderItemId, String reason) {
        log.info("Cancelling order item: orderId={}, memberId={}, itemId={}, reason={}", orderId, memberId, orderItemId, reason);

        Order order = findOrderById(orderId);
        validateOrderOwnership(order, memberId);

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        productService.increaseStock(item.getProductId(), item.getProductOptionId(), item.getQuantity());
        item.cancel();

        log.info("Order item cancelled: orderId={}, itemId={}", orderId, orderItemId);
        return order;
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
        if (SecurityUtils.hasRole("ADMIN")) {
            return;
        }
        validateOrderOwnership(order, memberId);
    }

    private void restoreStockForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getStatus() != OrderItemStatus.CANCELLED) {
                productService.increaseStock(item.getProductId(), item.getProductOptionId(), item.getQuantity());
            }
        }
    }
}
