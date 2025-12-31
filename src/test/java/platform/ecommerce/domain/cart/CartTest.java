package platform.ecommerce.domain.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import platform.ecommerce.exception.EntityNotFoundException;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Cart aggregate.
 */
class CartTest {

    private Cart cart;
    private static final Long MEMBER_ID = 1L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long OPTION_ID = 10L;

    @BeforeEach
    void setUp() {
        cart = Cart.builder()
                .memberId(MEMBER_ID)
                .build();
    }

    @Nested
    @DisplayName("Cart Creation")
    class CartCreation {

        @Test
        @DisplayName("should create cart for member")
        void createCart() {
            // when
            Cart newCart = Cart.builder()
                    .memberId(MEMBER_ID)
                    .build();

            // then
            assertThat(newCart.getMemberId()).isEqualTo(MEMBER_ID);
        }

        @Test
        @DisplayName("should initialize with empty items list")
        void initializeWithEmptyItems() {
            // when
            Cart newCart = Cart.builder()
                    .memberId(MEMBER_ID)
                    .build();

            // then
            assertThat(newCart.getItems()).isEmpty();
            assertThat(newCart.isEmpty()).isTrue();
            assertThat(newCart.getItemCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Add Item")
    class AddItem {

        @Test
        @DisplayName("should add new item to cart")
        void addNewItem() {
            // when
            CartItem item = cart.addItem(PRODUCT_ID, OPTION_ID, 2);

            // then
            assertThat(cart.getItems()).hasSize(1);
            assertThat(item.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(item.getProductOptionId()).isEqualTo(OPTION_ID);
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getAddedAt()).isNotNull();
        }

        @Test
        @DisplayName("should add item without option")
        void addItemWithoutOption() {
            // when
            CartItem item = cart.addItem(PRODUCT_ID, null, 1);

            // then
            assertThat(item.getProductOptionId()).isNull();
            assertThat(item.getQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("should merge quantity when adding duplicate product+option")
        void mergeQuantityForDuplicate() {
            // given
            cart.addItem(PRODUCT_ID, OPTION_ID, 2);

            // when
            CartItem item = cart.addItem(PRODUCT_ID, OPTION_ID, 3);

            // then
            assertThat(cart.getItems()).hasSize(1);
            assertThat(item.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("should not merge when option differs")
        void noMergeForDifferentOption() {
            // given
            cart.addItem(PRODUCT_ID, OPTION_ID, 2);

            // when
            cart.addItem(PRODUCT_ID, 20L, 3);

            // then
            assertThat(cart.getItems()).hasSize(2);
        }

        @Test
        @DisplayName("should throw exception when quantity is zero or negative")
        void throwOnInvalidQuantity() {
            // when & then
            assertThatThrownBy(() -> cart.addItem(PRODUCT_ID, OPTION_ID, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");

            assertThatThrownBy(() -> cart.addItem(PRODUCT_ID, OPTION_ID, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw exception when quantity exceeds maximum")
        void throwOnExceedingMaxQuantity() {
            // when & then
            assertThatThrownBy(() -> cart.addItem(PRODUCT_ID, OPTION_ID, 100))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("should throw exception when merged quantity exceeds maximum")
        void throwOnMergedQuantityExceedingMax() {
            // given
            cart.addItem(PRODUCT_ID, OPTION_ID, 90);

            // when & then
            assertThatThrownBy(() -> cart.addItem(PRODUCT_ID, OPTION_ID, 15))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("Update Quantity")
    class UpdateQuantity {

        @Test
        @DisplayName("should update item quantity")
        void updateItemQuantity() {
            // given
            CartItem item = cart.addItem(PRODUCT_ID, OPTION_ID, 2);
            ReflectionTestUtils.setField(item, "id", 1L);

            // when
            cart.updateItemQuantity(1L, 5);

            // then
            assertThat(item.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("should throw exception for invalid quantity")
        void throwOnInvalidQuantity() {
            // given
            CartItem item = cart.addItem(PRODUCT_ID, OPTION_ID, 2);
            ReflectionTestUtils.setField(item, "id", 1L);

            // when & then
            assertThatThrownBy(() -> cart.updateItemQuantity(1L, 0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> cart.updateItemQuantity(1L, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw exception for quantity exceeding maximum")
        void throwOnExceedingMaxQuantity() {
            // given
            CartItem item = cart.addItem(PRODUCT_ID, OPTION_ID, 2);
            ReflectionTestUtils.setField(item, "id", 1L);

            // when & then
            assertThatThrownBy(() -> cart.updateItemQuantity(1L, 100))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void throwOnItemNotFound() {
            // when & then
            assertThatThrownBy(() -> cart.updateItemQuantity(999L, 5))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Remove Item")
    class RemoveItem {

        @Test
        @DisplayName("should remove item from cart")
        void removeItem() {
            // given
            CartItem item = cart.addItem(PRODUCT_ID, OPTION_ID, 2);
            ReflectionTestUtils.setField(item, "id", 1L);
            cart.addItem(200L, null, 1);

            // when
            cart.removeItem(1L);

            // then
            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.findItem(PRODUCT_ID, OPTION_ID)).isEmpty();
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void throwOnItemNotFound() {
            // when & then
            assertThatThrownBy(() -> cart.removeItem(999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Clear Cart")
    class ClearCart {

        @Test
        @DisplayName("should remove all items from cart")
        void clearAllItems() {
            // given
            cart.addItem(PRODUCT_ID, OPTION_ID, 2);
            cart.addItem(200L, null, 3);
            assertThat(cart.getItems()).hasSize(2);

            // when
            cart.clear();

            // then
            assertThat(cart.getItems()).isEmpty();
            assertThat(cart.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("should work on already empty cart")
        void clearEmptyCart() {
            // when
            cart.clear();

            // then
            assertThat(cart.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Calculations")
    class Calculations {

        @Test
        @DisplayName("should calculate item count correctly")
        void calculateItemCount() {
            // given
            cart.addItem(PRODUCT_ID, OPTION_ID, 2);
            cart.addItem(200L, null, 3);

            // when
            int itemCount = cart.getItemCount();

            // then
            assertThat(itemCount).isEqualTo(5);
        }

        @Test
        @DisplayName("should calculate unique item count correctly")
        void calculateUniqueItemCount() {
            // given
            cart.addItem(PRODUCT_ID, OPTION_ID, 2);
            cart.addItem(200L, null, 3);
            cart.addItem(PRODUCT_ID, OPTION_ID, 1); // Merges with first

            // when
            int uniqueCount = cart.getUniqueItemCount();

            // then
            assertThat(uniqueCount).isEqualTo(2);
        }

        @Test
        @DisplayName("should return zero count for empty cart")
        void zeroCountForEmptyCart() {
            // then
            assertThat(cart.getItemCount()).isZero();
            assertThat(cart.getUniqueItemCount()).isZero();
        }
    }

    @Nested
    @DisplayName("CartItem Matching")
    class CartItemMatching {

        @Test
        @DisplayName("should match same product and option")
        void matchSameProductAndOption() {
            // given
            CartItem item = cart.addItem(PRODUCT_ID, OPTION_ID, 1);

            // when & then
            assertThat(item.matches(PRODUCT_ID, OPTION_ID)).isTrue();
        }

        @Test
        @DisplayName("should match when both options are null")
        void matchBothOptionsNull() {
            // given
            CartItem item = cart.addItem(PRODUCT_ID, null, 1);

            // when & then
            assertThat(item.matches(PRODUCT_ID, null)).isTrue();
        }

        @Test
        @DisplayName("should not match different product")
        void noMatchDifferentProduct() {
            // given
            CartItem item = cart.addItem(PRODUCT_ID, OPTION_ID, 1);

            // when & then
            assertThat(item.matches(200L, OPTION_ID)).isFalse();
        }

        @Test
        @DisplayName("should not match different option")
        void noMatchDifferentOption() {
            // given
            CartItem item = cart.addItem(PRODUCT_ID, OPTION_ID, 1);

            // when & then
            assertThat(item.matches(PRODUCT_ID, 20L)).isFalse();
        }

        @Test
        @DisplayName("should not match when one option is null")
        void noMatchOneOptionNull() {
            // given
            CartItem itemWithOption = cart.addItem(PRODUCT_ID, OPTION_ID, 1);

            // when & then
            assertThat(itemWithOption.matches(PRODUCT_ID, null)).isFalse();
        }
    }
}
