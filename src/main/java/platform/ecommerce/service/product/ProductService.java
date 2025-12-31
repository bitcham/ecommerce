package platform.ecommerce.service.product;

import org.springframework.data.domain.Pageable;
import platform.ecommerce.dto.request.product.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.dto.response.product.*;

/**
 * Product service interface.
 */
public interface ProductService {

    /**
     * Create a new product.
     */
    ProductResponse createProduct(Long sellerId, ProductCreateRequest request);

    /**
     * Get product by ID.
     */
    ProductResponse getProduct(Long productId);

    /**
     * Get product detail with options and images.
     */
    ProductDetailResponse getProductDetail(Long productId);

    /**
     * Search products with conditions.
     */
    PageResponse<ProductResponse> searchProducts(ProductSearchCondition condition, Pageable pageable);

    /**
     * Update product details.
     */
    ProductResponse updateProduct(Long productId, ProductUpdateRequest request);

    /**
     * Publish product (DRAFT -> ACTIVE).
     */
    ProductResponse publishProduct(Long productId);

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
     */
    ProductOptionResponse addOption(Long productId, ProductOptionRequest request);

    /**
     * Remove option from product.
     */
    void removeOption(Long productId, Long optionId);

    /**
     * Update option stock.
     */
    ProductOptionResponse updateOptionStock(Long productId, Long optionId, int stock);

    /**
     * Add image to product.
     */
    ProductImageResponse addImage(Long productId, String imageUrl, String altText);

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
