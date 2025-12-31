package platform.ecommerce.service;

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
import platform.ecommerce.domain.product.*;
import platform.ecommerce.dto.request.product.*;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.product.*;
import platform.ecommerce.exception.*;
import platform.ecommerce.repository.product.ProductRepository;
import platform.ecommerce.service.product.ProductServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ProductService unit tests.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Nested
    @DisplayName("Create Product")
    class CreateProduct {

        @Test
        @DisplayName("Should create product successfully")
        void createProduct_withValidRequest_shouldSucceed() {
            // given
            Long sellerId = 1L;
            ProductCreateRequest request = ProductCreateRequest.builder()
                    .name("Test Product")
                    .description("Description")
                    .basePrice(new BigDecimal("10000"))
                    .categoryId(1L)
                    .build();

            Product product = createProduct();
            setId(product, 1L);

            given(productRepository.save(any(Product.class))).willReturn(product);

            // when
            ProductResponse result = productService.createProduct(sellerId, request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Test Product");
            assertThat(result.status()).isEqualTo(ProductStatus.DRAFT);
        }
    }

    @Nested
    @DisplayName("Get Product")
    class GetProduct {

        @Test
        @DisplayName("Should return product when found")
        void getProduct_withValidId_shouldReturnProduct() {
            // given
            Long productId = 1L;
            Product product = createProduct();
            setId(product, productId);

            given(productRepository.findByIdNotDeleted(productId)).willReturn(Optional.of(product));

            // when
            ProductResponse result = productService.getProduct(productId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(productId);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void getProduct_withInvalidId_shouldThrowException() {
            // given
            Long productId = 999L;
            given(productRepository.findByIdNotDeleted(productId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.getProduct(productId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Search Products")
    class SearchProducts {

        @Test
        @DisplayName("Should return paged products")
        void searchProducts_withCondition_shouldReturnPagedResult() {
            // given
            ProductSearchCondition condition = ProductSearchCondition.empty();
            Pageable pageable = PageRequest.of(0, 10);

            Product product = createProduct();
            setId(product, 1L);
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            given(productRepository.searchProducts(condition, pageable)).willReturn(productPage);

            // when
            PageResponse<ProductResponse> result = productService.searchProducts(condition, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Update Product")
    class UpdateProduct {

        @Test
        @DisplayName("Should update product successfully")
        void updateProduct_withValidRequest_shouldSucceed() {
            // given
            Long productId = 1L;
            Product product = createProduct();
            setId(product, productId);

            ProductUpdateRequest request = ProductUpdateRequest.builder()
                    .name("Updated Name")
                    .basePrice(new BigDecimal("20000"))
                    .build();

            given(productRepository.findByIdNotDeleted(productId)).willReturn(Optional.of(product));

            // when
            ProductResponse result = productService.updateProduct(productId, request);

            // then
            assertThat(result).isNotNull();
            assertThat(product.getName()).isEqualTo("Updated Name");
        }
    }

    @Nested
    @DisplayName("Publish Product")
    class PublishProduct {

        @Test
        @DisplayName("Should publish product with options")
        void publishProduct_withOptions_shouldSucceed() {
            // given
            Long productId = 1L;
            Product product = createProduct();
            setId(product, productId);
            product.addOption(OptionType.SIZE, "M", BigDecimal.ZERO, 10);

            given(productRepository.findByIdNotDeleted(productId)).willReturn(Optional.of(product));

            // when
            ProductResponse result = productService.publishProduct(productId);

            // then
            assertThat(result).isNotNull();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw exception when publishing without options")
        void publishProduct_withoutOptions_shouldThrowException() {
            // given
            Long productId = 1L;
            Product product = createProduct();
            setId(product, productId);

            given(productRepository.findByIdNotDeleted(productId)).willReturn(Optional.of(product));

            // when & then
            assertThatThrownBy(() -> productService.publishProduct(productId))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("Option Management")
    class OptionManagement {

        @Test
        @DisplayName("Should add option successfully")
        void addOption_withValidRequest_shouldSucceed() {
            // given
            Long productId = 1L;
            Product product = createProduct();
            setId(product, productId);

            ProductOptionRequest request = ProductOptionRequest.builder()
                    .optionType(OptionType.COLOR)
                    .optionValue("Red")
                    .additionalPrice(BigDecimal.ZERO)
                    .stock(10)
                    .build();

            given(productRepository.findByIdNotDeleted(productId)).willReturn(Optional.of(product));

            // when
            ProductOptionResponse result = productService.addOption(productId, request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.optionValue()).isEqualTo("Red");
            assertThat(product.getOptions()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Stock Management")
    class StockManagement {

        @Test
        @DisplayName("Should decrease stock successfully")
        void decreaseStock_withSufficientStock_shouldSucceed() {
            // given
            Long productId = 1L;
            Long optionId = 1L;
            Product product = createProduct();
            setId(product, productId);

            ProductOption option = product.addOption(OptionType.SIZE, "M", BigDecimal.ZERO, 10);
            setId(option, optionId);

            given(productRepository.findByIdNotDeleted(productId)).willReturn(Optional.of(product));

            // when
            productService.decreaseStock(productId, optionId, 3);

            // then
            assertThat(option.getStock()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should throw exception for insufficient stock")
        void decreaseStock_insufficientStock_shouldThrowException() {
            // given
            Long productId = 1L;
            Long optionId = 1L;
            Product product = createProduct();
            setId(product, productId);

            ProductOption option = product.addOption(OptionType.SIZE, "M", BigDecimal.ZERO, 5);
            setId(option, optionId);

            given(productRepository.findByIdNotDeleted(productId)).willReturn(Optional.of(product));

            // when & then
            assertThatThrownBy(() -> productService.decreaseStock(productId, optionId, 10))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("Image Management")
    class ImageManagement {

        @Test
        @DisplayName("Should add image successfully")
        void addImage_shouldSucceed() {
            // given
            Long productId = 1L;
            Product product = createProduct();
            setId(product, productId);

            given(productRepository.findByIdNotDeleted(productId)).willReturn(Optional.of(product));

            // when
            ProductImageResponse result = productService.addImage(productId, "http://example.com/image.jpg", "Alt");

            // then
            assertThat(result).isNotNull();
            assertThat(result.imageUrl()).isEqualTo("http://example.com/image.jpg");
            assertThat(product.getImages()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Delete Product")
    class DeleteProduct {

        @Test
        @DisplayName("Should soft delete product")
        void deleteProduct_shouldMarkAsDeleted() {
            // given
            Long productId = 1L;
            Product product = createProduct();
            setId(product, productId);

            given(productRepository.findByIdNotDeleted(productId)).willReturn(Optional.of(product));

            // when
            productService.deleteProduct(productId);

            // then
            assertThat(product.getDeletedAt()).isNotNull();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }
    }

    // ========== Helper Methods ==========

    private Product createProduct() {
        return Product.builder()
                .name("Test Product")
                .description("Description")
                .basePrice(new BigDecimal("10000"))
                .sellerId(1L)
                .build();
    }

    private void setId(Object entity, Long id) {
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "id", id);
    }
}
