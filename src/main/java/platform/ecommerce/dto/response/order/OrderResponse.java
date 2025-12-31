package platform.ecommerce.dto.response.order;

import lombok.Builder;
import platform.ecommerce.domain.order.OrderStatus;
import platform.ecommerce.domain.order.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order response DTO.
 */
@Builder
public record OrderResponse(
        Long id,
        String orderNumber,
        Long memberId,
        OrderStatus status,
        ShippingAddressResponse shippingAddress,
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        String trackingNumber,
        LocalDateTime paidAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt
) {
}
