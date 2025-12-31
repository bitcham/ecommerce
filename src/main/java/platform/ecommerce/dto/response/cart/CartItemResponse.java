package platform.ecommerce.dto.response.cart;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cart item response DTO with product details.
 */
@Builder
public record CartItemResponse(
        Long id,
        Long productId,
        Long productOptionId,
        String productName,
        String optionName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal,
        boolean available,
        LocalDateTime addedAt
) {
}
