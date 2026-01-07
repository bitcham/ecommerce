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
 * Note: @SQLRestriction on Product entity automatically filters deleted records.
 */
public interface ProductRepository extends JpaRepository<Product, Long>, ProductQueryRepository {

    /**
     * Find products by seller.
     */
    List<Product> findBySellerId(Long sellerId);

    /**
     * Find products by category.
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Find products by status.
     */
    List<Product> findByStatus(ProductStatus status);

    /**
     * Check if product name exists for seller.
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.name = :name AND p.sellerId = :sellerId")
    boolean existsByNameAndSellerId(@Param("name") String name, @Param("sellerId") Long sellerId);

    // ========== Admin Methods (bypass @SQLRestriction) ==========

    /**
     * Find product by ID including deleted (for admin).
     */
    @Query(value = "SELECT * FROM product WHERE id = :id", nativeQuery = true)
    Optional<Product> findByIdIncludingDeleted(@Param("id") Long id);

    /**
     * Find all deleted products (for admin).
     */
    @Query(value = "SELECT * FROM product WHERE deleted_at IS NOT NULL", nativeQuery = true)
    List<Product> findAllDeleted();
}
