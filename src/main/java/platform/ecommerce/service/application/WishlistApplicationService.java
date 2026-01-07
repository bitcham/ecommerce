package platform.ecommerce.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.wishlist.WishlistItemResponse;
import platform.ecommerce.dto.response.wishlist.WishlistResponse;
import platform.ecommerce.service.wishlist.WishlistService;

/**
 * Wishlist application service.
 * Currently delegates to WishlistService.
 */
@Service
@RequiredArgsConstructor
public class WishlistApplicationService {

    private final WishlistService wishlistService;

    public WishlistResponse addToWishlist(Long memberId, Long productId) {
        return wishlistService.addToWishlist(memberId, productId);
    }

    public void removeFromWishlist(Long memberId, Long productId) {
        wishlistService.removeFromWishlist(memberId, productId);
    }

    public PageResponse<WishlistItemResponse> getMyWishlist(Long memberId, Pageable pageable) {
        return wishlistService.getMyWishlist(memberId, pageable);
    }

    public boolean isInWishlist(Long memberId, Long productId) {
        return wishlistService.isInWishlist(memberId, productId);
    }

    public long getWishlistCount(Long memberId) {
        return wishlistService.getWishlistCount(memberId);
    }
}
