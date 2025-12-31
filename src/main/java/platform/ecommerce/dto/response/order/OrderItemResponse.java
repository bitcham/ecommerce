package platform.ecommerce.dto.response.order;

import lombok.Builder;
import platform.ecommerce.domain.order.OrderItemStatus;

import java.math.BigDecimal;

/**
 * Order item response DTO.
 */
@Builder
public record OrderItemResponse(
        Long id,
        Long productId,
        Long productOptionId,
        String productName,
        String optionName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal,
        OrderItemStatus status
) {
}
