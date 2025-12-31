package platform.ecommerce.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.product.Product;
import platform.ecommerce.domain.product.ProductStatus;

import java.util.List;
import java.util.Optional;

/**
 * Product JPA repository.
 */
public interface ProductRepository extends JpaRepository<Product, Long>, ProductQueryRepository {

    /**
     * Find product by ID excluding deleted.
     */
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Product> findByIdNotDeleted(@Param("id") Long id);

    /**
     * Find products by seller.
     */
    List<Product> findBySellerIdAndDeletedAtIsNull(Long sellerId);

    /**
     * Find products by category.
     */
    List<Product> findByCategoryIdAndDeletedAtIsNull(Long categoryId);

    /**
     * Find products by status.
     */
    List<Product> findByStatusAndDeletedAtIsNull(ProductStatus status);

    /**
     * Check if product name exists for seller.
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.name = :name AND p.sellerId = :sellerId AND p.deletedAt IS NULL")
    boolean existsByNameAndSellerId(@Param("name") String name, @Param("sellerId") Long sellerId);
}
