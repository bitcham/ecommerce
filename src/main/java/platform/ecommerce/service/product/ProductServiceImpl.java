package platform.ecommerce.service.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.config.CacheConfig;
import platform.ecommerce.domain.product.*;
import platform.ecommerce.dto.request.product.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.dto.response.product.*;
import platform.ecommerce.exception.*;
import platform.ecommerce.repository.product.ProductRepository;

/**
 * Product service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(Long sellerId, ProductCreateRequest request) {
        log.info("Creating product for seller: {}", sellerId);

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .basePrice(request.basePrice())
                .sellerId(sellerId)
                .categoryId(request.categoryId())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created: id={}", savedProduct.getId());

        return toResponse(savedProduct);
    }

    @Override
    @Cacheable(value = CacheConfig.PRODUCT_CACHE, key = "#productId")
    public ProductResponse getProduct(Long productId) {
        Product product = findProductById(productId);
        return toResponse(product);
    }

    @Override
    @Cacheable(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = findProductById(productId);
        return toDetailResponse(product);
    }

    @Override
    public PageResponse<ProductResponse> searchProducts(ProductSearchCondition condition, Pageable pageable) {
        Page<Product> productPage = productRepository.searchProducts(condition, pageable);
        Page<ProductResponse> responsePage = productPage.map(this::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        log.info("Updating product: id={}", productId);

        Product product = findProductById(productId);
        product.update(request.name(), request.description(), request.basePrice(), request.categoryId());

        return toResponse(product);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public ProductResponse publishProduct(Long productId) {
        log.info("Publishing product: id={}", productId);

        Product product = findProductById(productId);
        product.publish();

        log.info("Product published: id={}", productId);
        return toResponse(product);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public void discontinueProduct(Long productId) {
        log.info("Discontinuing product: id={}", productId);

        Product product = findProductById(productId);
        product.discontinue();

        log.info("Product discontinued: id={}", productId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public void deleteProduct(Long productId) {
        log.info("Deleting product: id={}", productId);

        Product product = findProductById(productId);
        product.delete();

        log.info("Product deleted: id={}", productId);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    public ProductOptionResponse addOption(Long productId, ProductOptionRequest request) {
        log.info("Adding option to product: id={}", productId);

        Product product = findProductById(productId);
        ProductOption option = product.addOption(
                request.optionType(),
                request.optionValue(),
                request.additionalPrice(),
                request.stock()
        );

        return toOptionResponse(option);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    public void removeOption(Long productId, Long optionId) {
        log.info("Removing option from product: id={}, optionId={}", productId, optionId);

        Product product = findProductById(productId);
        product.removeOption(optionId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public ProductOptionResponse updateOptionStock(Long productId, Long optionId, int stock) {
        log.info("Updating option stock: productId={}, optionId={}, stock={}", productId, optionId, stock);

        Product product = findProductById(productId);
        ProductOption option = product.findOptionById(optionId);
        option.updateStock(stock);
        product.updateStatusByStock();

        return toOptionResponse(option);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    public ProductImageResponse addImage(Long productId, String imageUrl, String altText) {
        log.info("Adding image to product: id={}", productId);

        Product product = findProductById(productId);
        ProductImage image = product.addImage(imageUrl, altText);

        return toImageResponse(image);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    public void removeImage(Long productId, Long imageId) {
        log.info("Removing image from product: id={}, imageId={}", productId, imageId);

        Product product = findProductById(productId);
        product.removeImage(imageId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public void decreaseStock(Long productId, Long optionId, int quantity) {
        log.info("Decreasing stock: productId={}, optionId={}, quantity={}", productId, optionId, quantity);

        Product product = findProductById(productId);
        product.decreaseStock(optionId, quantity);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
            @CacheEvict(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    })
    public void increaseStock(Long productId, Long optionId, int quantity) {
        log.info("Increasing stock: productId={}, optionId={}, quantity={}", productId, optionId, quantity);

        Product product = findProductById(productId);
        product.increaseStock(optionId, quantity);
    }

    // ========== Private Helper Methods ==========

    private Product findProductById(Long productId) {
        return productRepository.findByIdNotDeleted(productId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private ProductResponse toResponse(Product product) {
        ProductImage mainImage = product.getMainImage();
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .sellerId(product.getSellerId())
                .categoryId(product.getCategoryId())
                .status(product.getStatus())
                .totalStock(product.getTotalStock())
                .mainImageUrl(mainImage != null ? mainImage.getImageUrl() : null)
                .createdAt(product.getCreatedAt())
                .build();
    }

    private ProductDetailResponse toDetailResponse(Product product) {
        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .sellerId(product.getSellerId())
                .categoryId(product.getCategoryId())
                .status(product.getStatus())
                .totalStock(product.getTotalStock())
                .options(product.getOptions().stream().map(this::toOptionResponse).toList())
                .images(product.getImages().stream().map(this::toImageResponse).toList())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductOptionResponse toOptionResponse(ProductOption option) {
        return ProductOptionResponse.builder()
                .id(option.getId())
                .optionType(option.getOptionType())
                .optionValue(option.getOptionValue())
                .additionalPrice(option.getAdditionalPrice())
                .stock(option.getStock())
                .displayOrder(option.getDisplayOrder())
                .inStock(option.isInStock())
                .build();
    }

    private ProductImageResponse toImageResponse(ProductImage image) {
        return ProductImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .displayOrder(image.getDisplayOrder())
                .build();
    }
}
