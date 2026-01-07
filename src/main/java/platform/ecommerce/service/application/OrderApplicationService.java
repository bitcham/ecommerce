package platform.ecommerce.service.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.order.Order;
import platform.ecommerce.domain.order.PaymentMethod;
import platform.ecommerce.dto.request.order.OrderCreateRequest;
import platform.ecommerce.dto.request.order.OrderSearchCondition;
import platform.ecommerce.dto.response.order.OrderResponse;
import platform.ecommerce.mapper.OrderMapper;
import platform.ecommerce.repository.MemberRepository;
import platform.ecommerce.service.email.EmailService;
import platform.ecommerce.service.notification.NotificationService;
import platform.ecommerce.service.order.OrderService;

/**
 * Application service for Order operations.
 * Handles DTO conversion, orchestration, and side effects (email, notification).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderApplicationService {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Transactional
    public OrderResponse createOrder(Long memberId, OrderCreateRequest request) {
        Order order = orderService.createOrder(memberId, request);
        return orderMapper.toResponse(order);
    }

    public OrderResponse getOrder(Long orderId, Long memberId) {
        Order order = orderService.getOrder(orderId, memberId);
        return orderMapper.toResponse(order);
    }

    public OrderResponse getOrderByNumber(String orderNumber, Long memberId) {
        Order order = orderService.getOrderByNumber(orderNumber, memberId);
        return orderMapper.toResponse(order);
    }

    public Page<OrderResponse> getMyOrders(Long memberId, Pageable pageable) {
        return orderService.getMyOrders(memberId, pageable)
                .map(orderMapper::toResponse);
    }

    public Page<OrderResponse> searchOrders(OrderSearchCondition condition, Pageable pageable) {
        return orderService.searchOrders(condition, pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional
    public OrderResponse processPayment(Long orderId, PaymentMethod paymentMethod, String transactionId) {
        Order order = orderService.processPayment(orderId, paymentMethod, transactionId);

        sendOrderConfirmationNotifications(order);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse startPreparing(Long orderId) {
        Order order = orderService.startPreparing(orderId);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse shipOrder(Long orderId, String trackingNumber) {
        Order order = orderService.shipOrder(orderId, trackingNumber);

        sendOrderShippedNotifications(order);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse deliverOrder(Long orderId) {
        Order order = orderService.deliverOrder(orderId);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long memberId, String reason) {
        Order order = orderService.cancelOrder(orderId, memberId, reason);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrderItem(Long orderId, Long memberId, Long orderItemId, String reason) {
        Order order = orderService.cancelOrderItem(orderId, memberId, orderItemId, reason);
        return orderMapper.toResponse(order);
    }

    // ========== Private Helper Methods ==========

    private void sendOrderConfirmationNotifications(Order order) {
        memberRepository.findById(order.getMemberId()).ifPresent(member -> {
            try {
                emailService.sendOrderConfirmationEmail(
                        member.getEmail(),
                        member.getName(),
                        order.getOrderNumber()
                );
            } catch (Exception e) {
                log.warn("Failed to send order confirmation email for order: {}", order.getOrderNumber(), e);
            }
        });

        try {
            notificationService.notifyOrderStatusChange(
                    order.getMemberId(),
                    order.getOrderNumber(),
                    "결제 완료"
            );
        } catch (Exception e) {
            log.warn("Failed to send order status notification for order: {}", order.getOrderNumber(), e);
        }
    }

    private void sendOrderShippedNotifications(Order order) {
        memberRepository.findById(order.getMemberId()).ifPresent(member -> {
            try {
                emailService.sendOrderShippedEmail(
                        member.getEmail(),
                        member.getName(),
                        order.getOrderNumber(),
                        order.getTrackingNumber()
                );
            } catch (Exception e) {
                log.warn("Failed to send order shipped email for order: {}", order.getOrderNumber(), e);
            }
        });

        try {
            notificationService.notifyDeliveryUpdate(
                    order.getMemberId(),
                    order.getOrderNumber(),
                    order.getTrackingNumber()
            );
        } catch (Exception e) {
            log.warn("Failed to send delivery notification for order: {}", order.getOrderNumber(), e);
        }
    }
}
