package platform.ecommerce.service.wishlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.wishlist.Wishlist;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.product.ProductResponse;
import platform.ecommerce.dto.response.wishlist.WishlistItemResponse;
import platform.ecommerce.dto.response.wishlist.WishlistResponse;
import platform.ecommerce.exception.DuplicateResourceException;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.mapper.WishlistMapper;
import platform.ecommerce.repository.WishlistRepository;
import platform.ecommerce.service.application.ProductApplicationService;

/**
 * Wishlist service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductApplicationService productApplicationService;
    private final WishlistMapper wishlistMapper;

    @Override
    @Transactional
    public WishlistResponse addToWishlist(Long memberId, Long productId) {
        log.info("Adding product to wishlist: memberId={}, productId={}", memberId, productId);

        // 1. Check duplicate first (cheaper than product lookup)
        if (wishlistRepository.existsByMemberIdAndProductId(memberId, productId)) {
            throw new DuplicateResourceException(ErrorCode.WISHLIST_ALREADY_EXISTS);
        }

        // 2. Verify product exists (will throw EntityNotFoundException if not found)
        productApplicationService.getProduct(productId);

        // 3. Save wishlist
        Wishlist wishlist = Wishlist.of(memberId, productId);
        Wishlist savedWishlist = wishlistRepository.save(wishlist);

        log.info("Product added to wishlist: memberId={}, productId={}", memberId, productId);
        return wishlistMapper.toResponse(savedWishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long memberId, Long productId) {
        log.info("Removing product from wishlist: memberId={}, productId={}", memberId, productId);

        Wishlist wishlist = wishlistRepository.findByMemberIdAndProductId(memberId, productId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WISHLIST_NOT_FOUND));

        wishlistRepository.delete(wishlist);
        log.info("Product removed from wishlist: memberId={}, productId={}", memberId, productId);
    }

    @Override
    public PageResponse<WishlistItemResponse> getMyWishlist(Long memberId, Pageable pageable) {
        log.info("Getting wishlist for member: {}", memberId);

        Page<Wishlist> wishlistPage = wishlistRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);

        Page<WishlistItemResponse> responsePage = wishlistPage.map(wishlist -> {
            try {
                ProductResponse product = productApplicationService.getProduct(wishlist.getProductId());
                return WishlistItemResponse.builder()
                        .wishlistId(wishlist.getId())
                        .productId(wishlist.getProductId())
                        .productName(product.name())
                        .productDescription(product.description())
                        .price(product.basePrice())
                        .mainImageUrl(product.mainImageUrl())
                        .productStatus(product.status())
                        .totalStock(product.totalStock())
                        .available(true)
                        .addedAt(wishlist.getCreatedAt())
                        .build();
            } catch (EntityNotFoundException e) {
                // Product was deleted - mark as unavailable
                return WishlistItemResponse.builder()
                        .wishlistId(wishlist.getId())
                        .productId(wishlist.getProductId())
                        .productName("Product unavailable")
                        .available(false)
                        .addedAt(wishlist.getCreatedAt())
                        .build();
            }
        });

        return PageResponse.of(responsePage);
    }

    @Override
    public boolean isInWishlist(Long memberId, Long productId) {
        return wishlistRepository.existsByMemberIdAndProductId(memberId, productId);
    }

    @Override
    public long getWishlistCount(Long memberId) {
        return wishlistRepository.countByMemberId(memberId);
    }
}
