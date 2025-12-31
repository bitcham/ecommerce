package platform.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import platform.ecommerce.domain.product.ProductStatus;
import platform.ecommerce.domain.wishlist.Wishlist;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.product.ProductResponse;
import platform.ecommerce.dto.response.wishlist.WishlistItemResponse;
import platform.ecommerce.dto.response.wishlist.WishlistResponse;
import platform.ecommerce.exception.DuplicateResourceException;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.repository.WishlistRepository;
import platform.ecommerce.service.product.ProductService;
import platform.ecommerce.service.wishlist.WishlistServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for WishlistService.
 */
@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private Wishlist testWishlist;
    private ProductResponse testProduct;
    private static final Long MEMBER_ID = 1L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long WISHLIST_ID = 1000L;

    @BeforeEach
    void setUp() {
        testWishlist = Wishlist.of(MEMBER_ID, PRODUCT_ID);
        ReflectionTestUtils.setField(testWishlist, "id", WISHLIST_ID);

        testProduct = ProductResponse.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .description("Test Description")
                .basePrice(BigDecimal.valueOf(29000))
                .sellerId(1L)
                .status(ProductStatus.ACTIVE)
                .totalStock(100)
                .mainImageUrl("http://example.com/image.jpg")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("addToWishlist")
    class AddToWishlist {

        @Test
        @DisplayName("should add product to wishlist successfully")
        void addProductToWishlist() {
            // given
            given(wishlistRepository.existsByMemberIdAndProductId(MEMBER_ID, PRODUCT_ID))
                    .willReturn(false);
            given(productService.getProduct(PRODUCT_ID)).willReturn(testProduct);
            given(wishlistRepository.save(any(Wishlist.class))).willAnswer(invocation -> {
                Wishlist wishlist = invocation.getArgument(0);
                ReflectionTestUtils.setField(wishlist, "id", WISHLIST_ID);
                return wishlist;
            });

            // when
            WishlistResponse response = wishlistService.addToWishlist(MEMBER_ID, PRODUCT_ID);

            // then
            assertThat(response.id()).isEqualTo(WISHLIST_ID);
            assertThat(response.memberId()).isEqualTo(MEMBER_ID);
            assertThat(response.productId()).isEqualTo(PRODUCT_ID);
            verify(wishlistRepository).save(any(Wishlist.class));
        }

        @Test
        @DisplayName("should throw exception when product already in wishlist")
        void throwOnDuplicateWishlist() {
            // given
            given(wishlistRepository.existsByMemberIdAndProductId(MEMBER_ID, PRODUCT_ID))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> wishlistService.addToWishlist(MEMBER_ID, PRODUCT_ID))
                    .isInstanceOf(DuplicateResourceException.class);
            verify(wishlistRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when product not found")
        void throwOnProductNotFound() {
            // given
            given(wishlistRepository.existsByMemberIdAndProductId(MEMBER_ID, PRODUCT_ID))
                    .willReturn(false);
            given(productService.getProduct(PRODUCT_ID))
                    .willThrow(new EntityNotFoundException(platform.ecommerce.exception.ErrorCode.PRODUCT_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> wishlistService.addToWishlist(MEMBER_ID, PRODUCT_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("removeFromWishlist")
    class RemoveFromWishlist {

        @Test
        @DisplayName("should remove product from wishlist successfully")
        void removeProductFromWishlist() {
            // given
            given(wishlistRepository.findByMemberIdAndProductId(MEMBER_ID, PRODUCT_ID))
                    .willReturn(Optional.of(testWishlist));

            // when
            wishlistService.removeFromWishlist(MEMBER_ID, PRODUCT_ID);

            // then
            verify(wishlistRepository).delete(testWishlist);
        }

        @Test
        @DisplayName("should throw exception when wishlist item not found")
        void throwOnWishlistNotFound() {
            // given
            given(wishlistRepository.findByMemberIdAndProductId(MEMBER_ID, PRODUCT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> wishlistService.removeFromWishlist(MEMBER_ID, PRODUCT_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getMyWishlist")
    class GetMyWishlist {

        @Test
        @DisplayName("should return paginated wishlist with product details")
        void returnPaginatedWishlist() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Wishlist> wishlistPage = new PageImpl<>(List.of(testWishlist), pageable, 1);

            given(wishlistRepository.findByMemberIdOrderByCreatedAtDesc(MEMBER_ID, pageable))
                    .willReturn(wishlistPage);
            given(productService.getProduct(PRODUCT_ID)).willReturn(testProduct);

            // when
            PageResponse<WishlistItemResponse> response = wishlistService.getMyWishlist(MEMBER_ID, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getPage().getTotalElements()).isEqualTo(1);

            WishlistItemResponse item = response.getContent().get(0);
            assertThat(item.productId()).isEqualTo(PRODUCT_ID);
            assertThat(item.productName()).isEqualTo("Test Product");
            assertThat(item.price()).isEqualByComparingTo(BigDecimal.valueOf(29000));
        }

        @Test
        @DisplayName("should return empty page when wishlist is empty")
        void returnEmptyPage() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Wishlist> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(wishlistRepository.findByMemberIdOrderByCreatedAtDesc(MEMBER_ID, pageable))
                    .willReturn(emptyPage);

            // when
            PageResponse<WishlistItemResponse> response = wishlistService.getMyWishlist(MEMBER_ID, pageable);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getPage().getTotalElements()).isZero();
        }

        @Test
        @DisplayName("should handle deleted products gracefully")
        void handleDeletedProducts() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Wishlist> wishlistPage = new PageImpl<>(List.of(testWishlist), pageable, 1);

            given(wishlistRepository.findByMemberIdOrderByCreatedAtDesc(MEMBER_ID, pageable))
                    .willReturn(wishlistPage);
            given(productService.getProduct(PRODUCT_ID))
                    .willThrow(new EntityNotFoundException(platform.ecommerce.exception.ErrorCode.PRODUCT_NOT_FOUND));

            // when
            PageResponse<WishlistItemResponse> response = wishlistService.getMyWishlist(MEMBER_ID, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            WishlistItemResponse item = response.getContent().get(0);
            assertThat(item.available()).isFalse();
            assertThat(item.productName()).isEqualTo("Product unavailable");
        }
    }

    @Nested
    @DisplayName("isInWishlist")
    class IsInWishlist {

        @Test
        @DisplayName("should return true when product is in wishlist")
        void returnTrueWhenInWishlist() {
            // given
            given(wishlistRepository.existsByMemberIdAndProductId(MEMBER_ID, PRODUCT_ID))
                    .willReturn(true);

            // when
            boolean result = wishlistService.isInWishlist(MEMBER_ID, PRODUCT_ID);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when product is not in wishlist")
        void returnFalseWhenNotInWishlist() {
            // given
            given(wishlistRepository.existsByMemberIdAndProductId(MEMBER_ID, PRODUCT_ID))
                    .willReturn(false);

            // when
            boolean result = wishlistService.isInWishlist(MEMBER_ID, PRODUCT_ID);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getWishlistCount")
    class GetWishlistCount {

        @Test
        @DisplayName("should return correct wishlist count")
        void returnCorrectCount() {
            // given
            given(wishlistRepository.countByMemberId(MEMBER_ID)).willReturn(5L);

            // when
            long count = wishlistService.getWishlistCount(MEMBER_ID);

            // then
            assertThat(count).isEqualTo(5);
        }

        @Test
        @DisplayName("should return zero when wishlist is empty")
        void returnZeroWhenEmpty() {
            // given
            given(wishlistRepository.countByMemberId(MEMBER_ID)).willReturn(0L);

            // when
            long count = wishlistService.getWishlistCount(MEMBER_ID);

            // then
            assertThat(count).isZero();
        }
    }
}
