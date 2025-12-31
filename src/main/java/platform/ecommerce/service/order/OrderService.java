package platform.ecommerce.service.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import platform.ecommerce.domain.order.PaymentMethod;
import platform.ecommerce.dto.request.order.OrderCreateRequest;
import platform.ecommerce.dto.request.order.OrderSearchCondition;
import platform.ecommerce.dto.response.order.OrderResponse;

/**
 * Service interface for Order operations.
 */
public interface OrderService {

    /**
     * Creates a new order.
     */
    OrderResponse createOrder(Long memberId, OrderCreateRequest request);

    /**
     * Gets an order by ID.
     * Verifies the member owns the order or is an admin.
     */
    OrderResponse getOrder(Long orderId, Long memberId);

    /**
     * Gets an order by order number.
     * Verifies the member owns the order or is an admin.
     */
    OrderResponse getOrderByNumber(String orderNumber, Long memberId);

    /**
     * Gets orders for a member.
     */
    Page<OrderResponse> getMyOrders(Long memberId, Pageable pageable);

    /**
     * Searches orders with conditions.
     */
    Page<OrderResponse> searchOrders(OrderSearchCondition condition, Pageable pageable);

    /**
     * Processes payment for an order.
     */
    OrderResponse processPayment(Long orderId, PaymentMethod paymentMethod, String transactionId);

    /**
     * Starts preparing an order (after payment confirmed).
     */
    OrderResponse startPreparing(Long orderId);

    /**
     * Ships an order with tracking number.
     */
    OrderResponse shipOrder(Long orderId, String trackingNumber);

    /**
     * Marks an order as delivered.
     */
    OrderResponse deliverOrder(Long orderId);

    /**
     * Cancels an order.
     * Verifies the member owns the order before cancellation.
     */
    OrderResponse cancelOrder(Long orderId, Long memberId, String reason);

    /**
     * Cancels a specific item in an order.
     * Verifies the member owns the order before cancellation.
     */
    OrderResponse cancelOrderItem(Long orderId, Long memberId, Long orderItemId, String reason);
}
