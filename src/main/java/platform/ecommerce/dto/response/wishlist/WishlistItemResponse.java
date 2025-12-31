package platform.ecommerce.dto.response.wishlist;

import lombok.Builder;
import platform.ecommerce.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for wishlist item with product details.
 */
@Builder
public record WishlistItemResponse(
        Long wishlistId,
        Long productId,
        String productName,
        String productDescription,
        BigDecimal price,
        String mainImageUrl,
        ProductStatus productStatus,
        int totalStock,
        boolean available,
        LocalDateTime addedAt
) {
}
