package platform.ecommerce.dto.response.cart;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cart response DTO with items and totals.
 */
@Builder
public record CartResponse(
        Long id,
        Long memberId,
        List<CartItemResponse> items,
        int itemCount,
        int uniqueItemCount,
        BigDecimal subtotal
) {
}
