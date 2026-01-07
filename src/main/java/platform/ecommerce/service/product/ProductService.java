package platform.ecommerce.service.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import platform.ecommerce.domain.product.Product;
import platform.ecommerce.domain.product.ProductImage;
import platform.ecommerce.domain.product.ProductOption;
import platform.ecommerce.dto.request.product.*;

/**
 * Product domain service interface.
 * Returns entities for ApplicationService to convert to DTOs.
 */
public interface ProductService {

    /**
     * Create a new product.
     * @return created Product entity
     */
    Product createProduct(Long sellerId, ProductCreateRequest request);

    /**
     * Get product by ID.
     * @return Product entity
     */
    Product getProduct(Long productId);

    /**
     * Search products with conditions.
     * @return page of Product entities
     */
    Page<Product> searchProducts(ProductSearchCondition condition, Pageable pageable);

    /**
     * Update product details.
     * @return updated Product entity
     */
    Product updateProduct(Long productId, ProductUpdateRequest request);

    /**
     * Publish product (DRAFT -> ACTIVE).
     * @return updated Product entity
     */
    Product publishProduct(Long productId);

    /**
     * Discontinue product.
     */
    void discontinueProduct(Long productId);

    /**
     * Delete product (soft delete).
     */
    void deleteProduct(Long productId);

    /**
     * Add option to product.
     * @return created ProductOption entity
     */
    ProductOption addOption(Long productId, ProductOptionRequest request);

    /**
     * Remove option from product.
     */
    void removeOption(Long productId, Long optionId);

    /**
     * Update option stock.
     * @return updated ProductOption entity
     */
    ProductOption updateOptionStock(Long productId, Long optionId, int stock);

    /**
     * Add image to product.
     * @return created ProductImage entity
     */
    ProductImage addImage(Long productId, String imageUrl, String altText);

    /**
     * Remove image from product.
     */
    void removeImage(Long productId, Long imageId);

    /**
     * Decrease stock (for order).
     */
    void decreaseStock(Long productId, Long optionId, int quantity);

    /**
     * Increase stock (for cancellation).
     */
    void increaseStock(Long productId, Long optionId, int quantity);
}
