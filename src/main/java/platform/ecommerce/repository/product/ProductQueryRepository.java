package platform.ecommerce.repository.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import platform.ecommerce.domain.product.Product;
import platform.ecommerce.dto.request.product.ProductSearchCondition;

/**
 * Product QueryDSL repository interface.
 */
public interface ProductQueryRepository {

    /**
     * Search products with dynamic conditions.
     */
    Page<Product> searchProducts(ProductSearchCondition condition, Pageable pageable);
}
