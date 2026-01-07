package platform.ecommerce.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import platform.ecommerce.config.CacheConfig;
import platform.ecommerce.domain.product.Product;
import platform.ecommerce.domain.product.ProductImage;
import platform.ecommerce.domain.product.ProductOption;
import platform.ecommerce.dto.request.product.*;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.product.*;
import platform.ecommerce.mapper.ProductMapper;
import platform.ecommerce.service.product.ProductService;

/**
 * Product application service.
 * Handles DTO conversion and caching using ProductMapper.
 */
@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    private final ProductService productService;
    private final ProductMapper productMapper;

    /**
     * Create a new product.
     */
    public ProductResponse createProduct(Long sellerId, ProductCreateRequest request) {
        Product product = productService.createProduct(sellerId, request);
        return productMapper.toResponse(product);
    }

    /**
     * Get product by ID with caching.
     */
    @Cacheable(value = CacheConfig.PRODUCT_CACHE, key = "#productId")
    public ProductResponse getProduct(Long productId) {
        Product product = productService.getProduct(productId);
        return productMapper.toResponse(product);
    }

    /**
     * Get product detail with options and images with caching.
     */
    @Cacheable(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productService.getProduct(productId);
        return productMapper.toDetailResponse(product);
    }

    /**
     * Search products with conditions.
     */
    public PageResponse<ProductResponse> searchProducts(ProductSearchCondition condition, Pageable pageable) {
        Page<Product> productPage = productService.searchProducts(condition, pageable);
        Page<ProductResponse> responsePage = productPage.map(productMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    /**
     * Update product details with cache eviction.
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productService.updateProduct(productId, request);
        return productMapper.toResponse(product);
    }

    /**
     * Publish product with cache eviction.
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public ProductResponse publishProduct(Long productId) {
        Product product = productService.publishProduct(productId);
        return productMapper.toResponse(product);
    }

    /**
     * Discontinue product with cache eviction.
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public void discontinueProduct(Long productId) {
        productService.discontinueProduct(productId);
    }

    /**
     * Delete product with cache eviction.
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public void deleteProduct(Long productId) {
        productService.deleteProduct(productId);
    }

    /**
     * Add option to product with cache eviction.
     */
    @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    public ProductOptionResponse addOption(Long productId, ProductOptionRequest request) {
        ProductOption option = productService.addOption(productId, request);
        return productMapper.toOptionResponse(option);
    }

    /**
     * Remove option from product with cache eviction.
     */
    @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    public void removeOption(Long productId, Long optionId) {
        productService.removeOption(productId, optionId);
    }

    /**
     * Update option stock with cache eviction.
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public ProductOptionResponse updateOptionStock(Long productId, Long optionId, int stock) {
        ProductOption option = productService.updateOptionStock(productId, optionId, stock);
        return productMapper.toOptionResponse(option);
    }

    /**
     * Add image to product with cache eviction.
     */
    @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    public ProductImageResponse addImage(Long productId, String imageUrl, String altText) {
        ProductImage image = productService.addImage(productId, imageUrl, altText);
        return productMapper.toImageResponse(image);
    }

    /**
     * Remove image from product with cache eviction.
     */
    @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    public void removeImage(Long productId, Long imageId) {
        productService.removeImage(productId, imageId);
    }

    /**
     * Decrease stock with cache eviction.
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public void decreaseStock(Long productId, Long optionId, int quantity) {
        productService.decreaseStock(productId, optionId, quantity);
    }

    /**
     * Increase stock with cache eviction.
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public void increaseStock(Long productId, Long optionId, int quantity) {
        productService.increaseStock(productId, optionId, quantity);
    }
}
