package platform.ecommerce.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import platform.ecommerce.domain.product.Product;
import platform.ecommerce.dto.request.product.ProductSearchCondition;
import platform.ecommerce.dto.request.product.ProductSortType;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.product.ProductResponse;
import platform.ecommerce.repository.product.ProductRepository;
import platform.ecommerce.service.product.ProductServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Product search enhancement tests.
 * Tests keyword search and sort functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Search Enhancement Tests")
class ProductSearchTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Nested
    @DisplayName("Keyword Search")
    class KeywordSearch {

        @Test
        @DisplayName("should search with keyword in name and description")
        void keywordSearchesNameAndDescription() {
            // given
            ProductSearchCondition condition = ProductSearchCondition.builder()
                    .keyword("gaming")
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Product product = createProduct("Gaming Keyboard", "Mechanical keyboard for gaming");
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            given(productRepository.searchProducts(any(), eq(pageable))).willReturn(productPage);

            // when
            PageResponse<ProductResponse> result = productService.searchProducts(condition, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).searchProducts(any(ProductSearchCondition.class), eq(pageable));
        }

        @Test
        @DisplayName("should return empty when no match found")
        void keywordSearchReturnsEmptyWhenNoMatch() {
            // given
            ProductSearchCondition condition = ProductSearchCondition.builder()
                    .keyword("nonexistent")
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            given(productRepository.searchProducts(any(), eq(pageable))).willReturn(emptyPage);

            // when
            PageResponse<ProductResponse> result = productService.searchProducts(condition, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("should return all products when keyword is empty")
        void emptyKeywordReturnsAllProducts() {
            // given
            ProductSearchCondition condition = ProductSearchCondition.builder()
                    .keyword("")
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Product product1 = createProduct("Product A", "Desc A");
            Product product2 = createProduct("Product B", "Desc B");
            Page<Product> productPage = new PageImpl<>(List.of(product1, product2), pageable, 2);

            given(productRepository.searchProducts(any(), eq(pageable))).willReturn(productPage);

            // when
            PageResponse<ProductResponse> result = productService.searchProducts(condition, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Sort Options")
    class SortOptions {

        @Test
        @DisplayName("should use LATEST sort by default")
        void defaultSortIsLatest() {
            // given
            ProductSearchCondition condition = ProductSearchCondition.empty();
            Pageable pageable = PageRequest.of(0, 10);

            Page<Product> productPage = new PageImpl<>(List.of(), pageable, 0);
            given(productRepository.searchProducts(any(), eq(pageable))).willReturn(productPage);

            // when
            productService.searchProducts(condition, pageable);

            // then
            ArgumentCaptor<ProductSearchCondition> captor = ArgumentCaptor.forClass(ProductSearchCondition.class);
            verify(productRepository).searchProducts(captor.capture(), eq(pageable));
            assertThat(captor.getValue().sortType()).isEqualTo(ProductSortType.LATEST);
        }

        @Test
        @DisplayName("should pass PRICE_LOW sort to repository")
        void sortByPriceLow() {
            // given
            ProductSearchCondition condition = ProductSearchCondition.builder()
                    .sortType(ProductSortType.PRICE_LOW)
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Page<Product> productPage = new PageImpl<>(List.of(), pageable, 0);
            given(productRepository.searchProducts(any(), eq(pageable))).willReturn(productPage);

            // when
            productService.searchProducts(condition, pageable);

            // then
            ArgumentCaptor<ProductSearchCondition> captor = ArgumentCaptor.forClass(ProductSearchCondition.class);
            verify(productRepository).searchProducts(captor.capture(), eq(pageable));
            assertThat(captor.getValue().sortType()).isEqualTo(ProductSortType.PRICE_LOW);
        }

        @Test
        @DisplayName("should pass PRICE_HIGH sort to repository")
        void sortByPriceHigh() {
            // given
            ProductSearchCondition condition = ProductSearchCondition.builder()
                    .sortType(ProductSortType.PRICE_HIGH)
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Page<Product> productPage = new PageImpl<>(List.of(), pageable, 0);
            given(productRepository.searchProducts(any(), eq(pageable))).willReturn(productPage);

            // when
            productService.searchProducts(condition, pageable);

            // then
            ArgumentCaptor<ProductSearchCondition> captor = ArgumentCaptor.forClass(ProductSearchCondition.class);
            verify(productRepository).searchProducts(captor.capture(), eq(pageable));
            assertThat(captor.getValue().sortType()).isEqualTo(ProductSortType.PRICE_HIGH);
        }

        @Test
        @DisplayName("should pass NAME_ASC sort to repository")
        void sortByNameAsc() {
            // given
            ProductSearchCondition condition = ProductSearchCondition.builder()
                    .sortType(ProductSortType.NAME_ASC)
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Page<Product> productPage = new PageImpl<>(List.of(), pageable, 0);
            given(productRepository.searchProducts(any(), eq(pageable))).willReturn(productPage);

            // when
            productService.searchProducts(condition, pageable);

            // then
            ArgumentCaptor<ProductSearchCondition> captor = ArgumentCaptor.forClass(ProductSearchCondition.class);
            verify(productRepository).searchProducts(captor.capture(), eq(pageable));
            assertThat(captor.getValue().sortType()).isEqualTo(ProductSortType.NAME_ASC);
        }
    }

    @Nested
    @DisplayName("Combined Filters")
    class CombinedFilters {

        @Test
        @DisplayName("should combine keyword with category filter")
        void keywordWithCategoryFilter() {
            // given
            ProductSearchCondition condition = ProductSearchCondition.builder()
                    .keyword("gaming")
                    .categoryId(1L)
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Product product = createProduct("Gaming Mouse", "High DPI gaming mouse");
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            given(productRepository.searchProducts(any(), eq(pageable))).willReturn(productPage);

            // when
            PageResponse<ProductResponse> result = productService.searchProducts(condition, pageable);

            // then
            ArgumentCaptor<ProductSearchCondition> captor = ArgumentCaptor.forClass(ProductSearchCondition.class);
            verify(productRepository).searchProducts(captor.capture(), eq(pageable));
            assertThat(captor.getValue().keyword()).isEqualTo("gaming");
            assertThat(captor.getValue().categoryId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should combine keyword with price range")
        void keywordWithPriceRange() {
            // given
            ProductSearchCondition condition = ProductSearchCondition.builder()
                    .keyword("laptop")
                    .minPrice(new BigDecimal("500000"))
                    .maxPrice(new BigDecimal("1500000"))
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Product product = createProduct("Gaming Laptop", "Powerful gaming laptop");
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            given(productRepository.searchProducts(any(), eq(pageable))).willReturn(productPage);

            // when
            PageResponse<ProductResponse> result = productService.searchProducts(condition, pageable);

            // then
            ArgumentCaptor<ProductSearchCondition> captor = ArgumentCaptor.forClass(ProductSearchCondition.class);
            verify(productRepository).searchProducts(captor.capture(), eq(pageable));
            assertThat(captor.getValue().keyword()).isEqualTo("laptop");
            assertThat(captor.getValue().minPrice()).isEqualByComparingTo("500000");
            assertThat(captor.getValue().maxPrice()).isEqualByComparingTo("1500000");
        }

        @Test
        @DisplayName("should combine keyword with sort option")
        void keywordWithSortOption() {
            // given
            ProductSearchCondition condition = ProductSearchCondition.builder()
                    .keyword("monitor")
                    .sortType(ProductSortType.PRICE_LOW)
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Product product = createProduct("LG Monitor", "4K UHD monitor");
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            given(productRepository.searchProducts(any(), eq(pageable))).willReturn(productPage);

            // when
            PageResponse<ProductResponse> result = productService.searchProducts(condition, pageable);

            // then
            ArgumentCaptor<ProductSearchCondition> captor = ArgumentCaptor.forClass(ProductSearchCondition.class);
            verify(productRepository).searchProducts(captor.capture(), eq(pageable));
            assertThat(captor.getValue().keyword()).isEqualTo("monitor");
            assertThat(captor.getValue().sortType()).isEqualTo(ProductSortType.PRICE_LOW);
        }
    }

    // ========== Helper Methods ==========

    private Product createProduct(String name, String description) {
        Product product = Product.builder()
                .name(name)
                .description(description)
                .basePrice(new BigDecimal("100000"))
                .sellerId(1L)
                .build();
        setId(product, 1L);
        return product;
    }

    private void setId(Object entity, Long id) {
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "id", id);
    }
}
