package platform.ecommerce.dto.response.wishlist;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Response DTO for a wishlist item.
 */
@Builder
public record WishlistResponse(
        Long id,
        Long memberId,
        Long productId,
        LocalDateTime createdAt
) {
}
