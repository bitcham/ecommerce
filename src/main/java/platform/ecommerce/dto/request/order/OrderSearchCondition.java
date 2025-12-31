package platform.ecommerce.dto.request.order;

import platform.ecommerce.domain.order.OrderStatus;

import java.time.LocalDateTime;

/**
 * Search condition for filtering orders.
 */
public record OrderSearchCondition(
        Long memberId,
        OrderStatus status,
        String orderNumber,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
    public OrderSearchCondition {
        // Validate date range if both provided
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    public static OrderSearchCondition ofMember(Long memberId) {
        return new OrderSearchCondition(memberId, null, null, null, null);
    }

    public static OrderSearchCondition ofMemberAndStatus(Long memberId, OrderStatus status) {
        return new OrderSearchCondition(memberId, status, null, null, null);
    }
}
