package platform.ecommerce.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import platform.ecommerce.exception.InvalidStateException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Product domain unit tests.
 */
@DisplayName("Product Domain Tests")
class ProductTest {

    @Nested
    @DisplayName("Product Creation")
    class ProductCreation {

        @Test
        @DisplayName("Should create product with valid data")
        void create_withValidData_shouldSucceed() {
            // when
            Product product = Product.builder()
                    .name("Test Product")
                    .description("Description")
                    .basePrice(new BigDecimal("10000"))
                    .sellerId(1L)
                    .categoryId(1L)
                    .build();

            // then
            assertThat(product.getName()).isEqualTo("Test Product");
            assertThat(product.getBasePrice()).isEqualByComparingTo("10000");
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        }

        @Test
        @DisplayName("Should throw exception for null name")
        void create_withNullName_shouldThrowException() {
            // when & then
            assertThatThrownBy(() -> Product.builder()
                    .name(null)
                    .basePrice(new BigDecimal("10000"))
                    .sellerId(1L)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("Should throw exception for negative price")
        void create_withNegativePrice_shouldThrowException() {
            // when & then
            assertThatThrownBy(() -> Product.builder()
                    .name("Test")
                    .basePrice(new BigDecimal("-100"))
                    .sellerId(1L)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Price");
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitions {

        @Test
        @DisplayName("Should publish draft product with options")
        void publish_draftWithOptions_shouldSucceed() {
            // given
            Product product = createDraftProduct();
            product.addOption(OptionType.COLOR, "Red", BigDecimal.ZERO, 10);

            // when
            product.publish();

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw exception when publishing without options")
        void publish_withoutOptions_shouldThrowException() {
            // given
            Product product = createDraftProduct();

            // when & then
            assertThatThrownBy(product::publish)
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("Should throw exception when publishing non-draft")
        void publish_activeProduct_shouldThrowException() {
            // given
            Product product = createActiveProduct();

            // when & then
            assertThatThrownBy(product::publish)
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("Should discontinue active product")
        void discontinue_activeProduct_shouldSucceed() {
            // given
            Product product = createActiveProduct();

            // when
            product.discontinue();

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("Should mark as sold out when all options have 0 stock")
        void updateStatusByStock_noStock_shouldMarkSoldOut() {
            // given
            Product product = createActiveProduct();
            product.getOptions().forEach(o -> o.updateStock(0));

            // when
            product.updateStatusByStock();

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }
    }

    @Nested
    @DisplayName("Option Management")
    class OptionManagement {

        @Test
        @DisplayName("Should add option successfully")
        void addOption_shouldSucceed() {
            // given
            Product product = createDraftProduct();

            // when
            ProductOption option = product.addOption(OptionType.SIZE, "M", BigDecimal.ZERO, 10);

            // then
            assertThat(product.getOptions()).hasSize(1);
            assertThat(option.getOptionValue()).isEqualTo("M");
            assertThat(option.getStock()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should calculate total stock from all options")
        void getTotalStock_multipleOptions_shouldSum() {
            // given
            Product product = createDraftProduct();
            product.addOption(OptionType.SIZE, "S", BigDecimal.ZERO, 10);
            product.addOption(OptionType.SIZE, "M", BigDecimal.ZERO, 20);
            product.addOption(OptionType.SIZE, "L", BigDecimal.ZERO, 30);

            // when
            int totalStock = product.getTotalStock();

            // then
            assertThat(totalStock).isEqualTo(60);
        }

        @Test
        @DisplayName("Should throw exception for negative stock")
        void addOption_negativeStock_shouldThrowException() {
            // given
            Product product = createDraftProduct();

            // when & then
            assertThatThrownBy(() -> product.addOption(OptionType.SIZE, "M", BigDecimal.ZERO, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Stock Management")
    class StockManagement {

        @Test
        @DisplayName("Should decrease stock on purchase")
        void decreaseStock_validQuantity_shouldSucceed() {
            // given
            Product product = createDraftProduct();
            ProductOption option = product.addOption(OptionType.SIZE, "M", BigDecimal.ZERO, 10);
            Long optionId = setId(option, 1L);

            // when
            product.decreaseStock(optionId, 3);

            // then
            assertThat(option.getStock()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should throw exception for insufficient stock")
        void decreaseStock_insufficientStock_shouldThrowException() {
            // given
            Product product = createDraftProduct();
            ProductOption option = product.addOption(OptionType.SIZE, "M", BigDecimal.ZERO, 5);
            Long optionId = setId(option, 1L);

            // when & then
            assertThatThrownBy(() -> product.decreaseStock(optionId, 10))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("Should increase stock on cancellation")
        void increaseStock_shouldSucceed() {
            // given
            Product product = createDraftProduct();
            ProductOption option = product.addOption(OptionType.SIZE, "M", BigDecimal.ZERO, 10);
            Long optionId = setId(option, 1L);

            // when
            product.increaseStock(optionId, 5);

            // then
            assertThat(option.getStock()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("Image Management")
    class ImageManagement {

        @Test
        @DisplayName("Should add image with order")
        void addImage_shouldSucceed() {
            // given
            Product product = createDraftProduct();

            // when
            ProductImage image = product.addImage("http://example.com/image.jpg", "Product Image");

            // then
            assertThat(product.getImages()).hasSize(1);
            assertThat(image.getImageUrl()).isEqualTo("http://example.com/image.jpg");
            assertThat(image.getDisplayOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should get main image as first in order")
        void getMainImage_multipleImages_shouldReturnFirst() {
            // given
            Product product = createDraftProduct();
            product.addImage("http://example.com/1.jpg", "First");
            product.addImage("http://example.com/2.jpg", "Second");

            // when
            ProductImage mainImage = product.getMainImage();

            // then
            assertThat(mainImage.getAltText()).isEqualTo("First");
        }
    }

    @Nested
    @DisplayName("Soft Delete")
    class SoftDelete {

        @Test
        @DisplayName("Should mark as deleted with timestamp")
        void delete_shouldSetDeletedAt() {
            // given
            Product product = createDraftProduct();

            // when
            product.delete();

            // then
            assertThat(product.getDeletedAt()).isNotNull();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("Should restore deleted product")
        void restore_deletedProduct_shouldSucceed() {
            // given
            Product product = createDraftProduct();
            product.delete();

            // when
            product.restore();

            // then
            assertThat(product.getDeletedAt()).isNull();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        }
    }

    // ========== Helper Methods ==========

    private Product createDraftProduct() {
        return Product.builder()
                .name("Test Product")
                .description("Description")
                .basePrice(new BigDecimal("10000"))
                .sellerId(1L)
                .build();
    }

    private Product createActiveProduct() {
        Product product = createDraftProduct();
        product.addOption(OptionType.COLOR, "Red", BigDecimal.ZERO, 10);
        product.publish();
        return product;
    }

    private Long setId(Object entity, Long id) {
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "id", id);
        return id;
    }
}
