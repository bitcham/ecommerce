package platform.ecommerce.service.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import platform.ecommerce.domain.order.Order;
import platform.ecommerce.domain.order.PaymentMethod;
import platform.ecommerce.dto.request.order.OrderCreateRequest;
import platform.ecommerce.dto.request.order.OrderSearchCondition;

/**
 * Domain service interface for Order operations.
 * Returns entities for use by application layer.
 */
public interface OrderService {

    /**
     * Creates a new order.
     */
    Order createOrder(Long memberId, OrderCreateRequest request);

    /**
     * Gets an order by ID.
     */
    Order getOrder(Long orderId);

    /**
     * Gets an order by ID with ownership validation.
     */
    Order getOrder(Long orderId, Long memberId);

    /**
     * Gets an order by order number with ownership validation.
     */
    Order getOrderByNumber(String orderNumber, Long memberId);

    /**
     * Gets orders for a member.
     */
    Page<Order> getMyOrders(Long memberId, Pageable pageable);

    /**
     * Searches orders with conditions.
     */
    Page<Order> searchOrders(OrderSearchCondition condition, Pageable pageable);

    /**
     * Processes payment for an order.
     */
    Order processPayment(Long orderId, PaymentMethod paymentMethod, String transactionId);

    /**
     * Starts preparing an order (after payment confirmed).
     */
    Order startPreparing(Long orderId);

    /**
     * Ships an order with tracking number.
     */
    Order shipOrder(Long orderId, String trackingNumber);

    /**
     * Marks an order as delivered.
     */
    Order deliverOrder(Long orderId);

    /**
     * Cancels an order.
     */
    Order cancelOrder(Long orderId, Long memberId, String reason);

    /**
     * Cancels a specific item in an order.
     */
    Order cancelOrderItem(Long orderId, Long memberId, Long orderItemId, String reason);
}
