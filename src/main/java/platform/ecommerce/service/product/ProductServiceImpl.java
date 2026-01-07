package platform.ecommerce.service.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.product.*;
import platform.ecommerce.dto.request.product.*;
import platform.ecommerce.exception.*;
import platform.ecommerce.repository.product.ProductRepository;

/**
 * Product domain service implementation.
 * Pure business logic - returns entities. No caching here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Product createProduct(Long sellerId, ProductCreateRequest request) {
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

        return savedProduct;
    }

    @Override
    public Product getProduct(Long productId) {
        return findProductById(productId);
    }

    @Override
    public Page<Product> searchProducts(ProductSearchCondition condition, Pageable pageable) {
        return productRepository.searchProducts(condition, pageable);
    }

    @Override
    @Transactional
    public Product updateProduct(Long productId, ProductUpdateRequest request) {
        log.info("Updating product: id={}", productId);

        Product product = findProductById(productId);
        product.update(request.name(), request.description(), request.basePrice(), request.categoryId());

        return product;
    }

    @Override
    @Transactional
    public Product publishProduct(Long productId) {
        log.info("Publishing product: id={}", productId);

        Product product = findProductById(productId);
        product.publish();

        log.info("Product published: id={}", productId);
        return product;
    }

    @Override
    @Transactional
    public void discontinueProduct(Long productId) {
        log.info("Discontinuing product: id={}", productId);

        Product product = findProductById(productId);
        product.discontinue();

        log.info("Product discontinued: id={}", productId);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        log.info("Deleting product: id={}", productId);

        Product product = findProductById(productId);
        product.delete();

        log.info("Product deleted: id={}", productId);
    }

    @Override
    @Transactional
    public ProductOption addOption(Long productId, ProductOptionRequest request) {
        log.info("Adding option to product: id={}", productId);

        Product product = findProductById(productId);
        return product.addOption(
                request.optionType(),
                request.optionValue(),
                request.additionalPrice(),
                request.stock()
        );
    }

    @Override
    @Transactional
    public void removeOption(Long productId, Long optionId) {
        log.info("Removing option from product: id={}, optionId={}", productId, optionId);

        Product product = findProductById(productId);
        product.removeOption(optionId);
    }

    @Override
    @Transactional
    public ProductOption updateOptionStock(Long productId, Long optionId, int stock) {
        log.info("Updating option stock: productId={}, optionId={}, stock={}", productId, optionId, stock);

        Product product = findProductById(productId);
        ProductOption option = product.findOptionById(optionId);
        option.updateStock(stock);
        product.updateStatusByStock();

        return option;
    }

    @Override
    @Transactional
    public ProductImage addImage(Long productId, String imageUrl, String altText) {
        log.info("Adding image to product: id={}", productId);

        Product product = findProductById(productId);
        return product.addImage(imageUrl, altText);
    }

    @Override
    @Transactional
    public void removeImage(Long productId, Long imageId) {
        log.info("Removing image from product: id={}, imageId={}", productId, imageId);

        Product product = findProductById(productId);
        product.removeImage(imageId);
    }

    @Override
    @Transactional
    public void decreaseStock(Long productId, Long optionId, int quantity) {
        log.info("Decreasing stock: productId={}, optionId={}, quantity={}", productId, optionId, quantity);

        Product product = findProductById(productId);
        product.decreaseStock(optionId, quantity);
    }

    @Override
    @Transactional
    public void increaseStock(Long productId, Long optionId, int quantity) {
        log.info("Increasing stock: productId={}, optionId={}, quantity={}", productId, optionId, quantity);

        Product product = findProductById(productId);
        product.increaseStock(optionId, quantity);
    }

    // ========== Private Helper Methods ==========

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
