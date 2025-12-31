package platform.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.dto.response.ApiResponse;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.wishlist.WishlistItemResponse;
import platform.ecommerce.dto.response.wishlist.WishlistResponse;
import platform.ecommerce.security.SecurityUtils;
import platform.ecommerce.service.wishlist.WishlistService;

/**
 * Wishlist REST controller.
 */
@Tag(name = "Wishlist", description = "Wishlist management API")
@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "Add to wishlist", description = "Add a product to wishlist")
    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<WishlistResponse> addToWishlist(
            @Parameter(description = "Product ID") @PathVariable Long productId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        WishlistResponse response = wishlistService.addToWishlist(memberId, productId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Remove from wishlist", description = "Remove a product from wishlist")
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void removeFromWishlist(
            @Parameter(description = "Product ID") @PathVariable Long productId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        wishlistService.removeFromWishlist(memberId, productId);
    }

    @Operation(summary = "Get my wishlist", description = "Get paginated wishlist for authenticated member")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<WishlistItemResponse>> getMyWishlist(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PageResponse<WishlistItemResponse> response = wishlistService.getMyWishlist(memberId, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Check wishlist", description = "Check if product is in wishlist")
    @GetMapping("/{productId}/check")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> isInWishlist(
            @Parameter(description = "Product ID") @PathVariable Long productId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        boolean result = wishlistService.isInWishlist(memberId, productId);
        return ApiResponse.success(result);
    }

    @Operation(summary = "Get wishlist count", description = "Get count of items in wishlist")
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> getWishlistCount() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        long count = wishlistService.getWishlistCount(memberId);
        return ApiResponse.success(count);
    }
}
