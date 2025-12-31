package platform.ecommerce.service.wishlist;

import org.springframework.data.domain.Pageable;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.wishlist.WishlistItemResponse;
import platform.ecommerce.dto.response.wishlist.WishlistResponse;

/**
 * Service interface for Wishlist operations.
 */
public interface WishlistService {

    /**
     * Adds a product to the member's wishlist.
     */
    WishlistResponse addToWishlist(Long memberId, Long productId);

    /**
     * Removes a product from the member's wishlist.
     */
    void removeFromWishlist(Long memberId, Long productId);

    /**
     * Gets the member's wishlist with pagination.
     */
    PageResponse<WishlistItemResponse> getMyWishlist(Long memberId, Pageable pageable);

    /**
     * Checks if a product is in the member's wishlist.
     */
    boolean isInWishlist(Long memberId, Long productId);

    /**
     * Gets the count of items in the member's wishlist.
     */
    long getWishlistCount(Long memberId);
}
