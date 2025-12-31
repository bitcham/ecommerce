package platform.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import platform.ecommerce.domain.cart.Cart;
import platform.ecommerce.domain.cart.CartItem;
import platform.ecommerce.domain.product.ProductStatus;
import platform.ecommerce.dto.request.cart.CartItemAddRequest;
import platform.ecommerce.dto.response.cart.CartItemResponse;
import platform.ecommerce.dto.response.cart.CartResponse;
import platform.ecommerce.dto.response.product.ProductDetailResponse;
import platform.ecommerce.dto.response.product.ProductOptionResponse;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.repository.cart.CartRepository;
import platform.ecommerce.service.cart.CartServiceImpl;
import platform.ecommerce.service.product.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for CartService.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private ProductDetailResponse testProduct;
    private static final Long MEMBER_ID = 1L;
    private static final Long CART_ID = 100L;
    private static final Long PRODUCT_ID = 200L;
    private static final Long OPTION_ID = 10L;

    @BeforeEach
    void setUp() {
        testCart = Cart.builder()
                .memberId(MEMBER_ID)
                .build();
        ReflectionTestUtils.setField(testCart, "id", CART_ID);

        ProductOptionResponse option = ProductOptionResponse.builder()
                .id(OPTION_ID)
                .optionValue("Size M")
                .additionalPrice(BigDecimal.valueOf(1000))
                .stock(50)
                .inStock(true)
                .build();

        testProduct = ProductDetailResponse.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .description("Test Description")
                .basePrice(BigDecimal.valueOf(29000))
                .sellerId(1L)
                .status(ProductStatus.ACTIVE)
                .totalStock(100)
                .options(List.of(option))
                .images(List.of())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getOrCreateCart")
    class GetOrCreateCart {

        @Test
        @DisplayName("should return existing cart for member")
        void returnExistingCart() {
            // given
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(testCart));

            // when
            CartResponse response = cartService.getOrCreateCart(MEMBER_ID);

            // then
            assertThat(response.id()).isEqualTo(CART_ID);
            assertThat(response.memberId()).isEqualTo(MEMBER_ID);
            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create new cart if none exists")
        void createNewCart() {
            // given
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.empty());
            given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> {
                Cart cart = invocation.getArgument(0);
                ReflectionTestUtils.setField(cart, "id", CART_ID);
                return cart;
            });

            // when
            CartResponse response = cartService.getOrCreateCart(MEMBER_ID);

            // then
            assertThat(response.memberId()).isEqualTo(MEMBER_ID);
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("addToCart")
    class AddToCart {

        @Test
        @DisplayName("should add new item to cart")
        void addNewItem() {
            // given
            CartItemAddRequest request = CartItemAddRequest.builder()
                    .productId(PRODUCT_ID)
                    .productOptionId(OPTION_ID)
                    .quantity(2)
                    .build();

            given(productService.getProductDetail(PRODUCT_ID)).willReturn(testProduct);
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(testCart));

            // when
            CartItemResponse response = cartService.addToCart(MEMBER_ID, request);

            // then
            assertThat(response.productId()).isEqualTo(PRODUCT_ID);
            assertThat(response.productOptionId()).isEqualTo(OPTION_ID);
            assertThat(response.quantity()).isEqualTo(2);
            assertThat(response.productName()).isEqualTo("Test Product");
            assertThat(response.optionName()).isEqualTo("Size M");
            // Base price 29000 + option 1000 = 30000
            assertThat(response.unitPrice()).isEqualByComparingTo(BigDecimal.valueOf(30000));
            // 30000 * 2 = 60000
            assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(60000));
        }

        @Test
        @DisplayName("should merge quantity for existing item")
        void mergeQuantityForExistingItem() {
            // given
            CartItem existingItem = testCart.addItem(PRODUCT_ID, OPTION_ID, 2);
            ReflectionTestUtils.setField(existingItem, "id", 50L);

            CartItemAddRequest request = CartItemAddRequest.builder()
                    .productId(PRODUCT_ID)
                    .productOptionId(OPTION_ID)
                    .quantity(3)
                    .build();

            given(productService.getProductDetail(PRODUCT_ID)).willReturn(testProduct);
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(testCart));

            // when
            CartItemResponse response = cartService.addToCart(MEMBER_ID, request);

            // then
            assertThat(response.quantity()).isEqualTo(5); // 2 + 3
            assertThat(testCart.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("should throw exception when product option not found")
        void throwOnInvalidOption() {
            // given
            CartItemAddRequest request = CartItemAddRequest.builder()
                    .productId(PRODUCT_ID)
                    .productOptionId(999L) // Invalid option
                    .quantity(1)
                    .build();

            given(productService.getProductDetail(PRODUCT_ID)).willReturn(testProduct);

            // when & then
            assertThatThrownBy(() -> cartService.addToCart(MEMBER_ID, request))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should create cart if it doesn't exist")
        void createCartIfNotExists() {
            // given
            CartItemAddRequest request = CartItemAddRequest.builder()
                    .productId(PRODUCT_ID)
                    .productOptionId(OPTION_ID)
                    .quantity(1)
                    .build();

            given(productService.getProductDetail(PRODUCT_ID)).willReturn(testProduct);
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.empty());
            given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> {
                Cart cart = invocation.getArgument(0);
                ReflectionTestUtils.setField(cart, "id", CART_ID);
                return cart;
            });

            // when
            CartItemResponse response = cartService.addToCart(MEMBER_ID, request);

            // then
            assertThat(response.productId()).isEqualTo(PRODUCT_ID);
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("updateQuantity")
    class UpdateQuantity {

        @Test
        @DisplayName("should update item quantity")
        void updateItemQuantity() {
            // given
            CartItem item = testCart.addItem(PRODUCT_ID, OPTION_ID, 2);
            ReflectionTestUtils.setField(item, "id", 50L);

            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(testCart));
            given(productService.getProductDetail(PRODUCT_ID)).willReturn(testProduct);

            // when
            CartItemResponse response = cartService.updateQuantity(MEMBER_ID, 50L, 5);

            // then
            assertThat(response.quantity()).isEqualTo(5);
            assertThat(item.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("should throw exception when cart not found")
        void throwOnCartNotFound() {
            // given
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.updateQuantity(MEMBER_ID, 50L, 5))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void throwOnItemNotFound() {
            // given
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(testCart));

            // when & then
            assertThatThrownBy(() -> cartService.updateQuantity(MEMBER_ID, 999L, 5))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("removeFromCart")
    class RemoveFromCart {

        @Test
        @DisplayName("should remove item from cart")
        void removeItem() {
            // given
            CartItem item = testCart.addItem(PRODUCT_ID, OPTION_ID, 2);
            ReflectionTestUtils.setField(item, "id", 50L);
            assertThat(testCart.getItems()).hasSize(1);

            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(testCart));

            // when
            cartService.removeFromCart(MEMBER_ID, 50L);

            // then
            assertThat(testCart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void throwOnItemNotFound() {
            // given
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(testCart));

            // when & then
            assertThatThrownBy(() -> cartService.removeFromCart(MEMBER_ID, 999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("clearCart")
    class ClearCart {

        @Test
        @DisplayName("should clear all items from cart")
        void clearAllItems() {
            // given
            testCart.addItem(PRODUCT_ID, OPTION_ID, 2);
            testCart.addItem(300L, null, 1);
            assertThat(testCart.getItems()).hasSize(2);

            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(testCart));

            // when
            cartService.clearCart(MEMBER_ID);

            // then
            assertThat(testCart.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("getCartSummary")
    class GetCartSummary {

        @Test
        @DisplayName("should return cart with product details and totals")
        void returnCartWithDetails() {
            // given
            CartItem item = testCart.addItem(PRODUCT_ID, OPTION_ID, 2);
            ReflectionTestUtils.setField(item, "id", 50L);

            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(testCart));
            given(productService.getProductDetail(PRODUCT_ID)).willReturn(testProduct);

            // when
            CartResponse response = cartService.getCartSummary(MEMBER_ID);

            // then
            assertThat(response.id()).isEqualTo(CART_ID);
            assertThat(response.items()).hasSize(1);
            assertThat(response.itemCount()).isEqualTo(2);
            assertThat(response.uniqueItemCount()).isEqualTo(1);

            CartItemResponse itemResponse = response.items().get(0);
            assertThat(itemResponse.productName()).isEqualTo("Test Product");
            assertThat(itemResponse.optionName()).isEqualTo("Size M");
            assertThat(itemResponse.unitPrice()).isEqualByComparingTo(BigDecimal.valueOf(30000));
            assertThat(itemResponse.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(60000));

            // Total should be calculated
            assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(60000));
        }

        @Test
        @DisplayName("should handle unavailable products gracefully")
        void handleUnavailableProducts() {
            // given
            CartItem item = testCart.addItem(PRODUCT_ID, OPTION_ID, 2);
            ReflectionTestUtils.setField(item, "id", 50L);

            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(testCart));
            given(productService.getProductDetail(PRODUCT_ID))
                    .willThrow(new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

            // when
            CartResponse response = cartService.getCartSummary(MEMBER_ID);

            // then
            assertThat(response.items()).hasSize(1);
            CartItemResponse itemResponse = response.items().get(0);
            assertThat(itemResponse.available()).isFalse();
            assertThat(itemResponse.productName()).isEqualTo("Product unavailable");
        }

        @Test
        @DisplayName("should return empty cart if none exists")
        void returnEmptyCartIfNoneExists() {
            // given
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.empty());

            // when
            CartResponse response = cartService.getCartSummary(MEMBER_ID);

            // then
            assertThat(response.memberId()).isEqualTo(MEMBER_ID);
            assertThat(response.items()).isEmpty();
            assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
