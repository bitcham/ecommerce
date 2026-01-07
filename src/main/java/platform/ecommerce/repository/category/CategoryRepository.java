package platform.ecommerce.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.category.Category;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category aggregate root.
 * Note: @SQLRestriction on Category entity automatically filters deleted records.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL ORDER BY c.displayOrder")
    List<Category> findRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parentId = :parentId ORDER BY c.displayOrder")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    @Query("SELECT c FROM Category c ORDER BY c.depth, c.displayOrder")
    List<Category> findAllActive();

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.parentId = :parentId")
    boolean hasChildren(@Param("parentId") Long parentId);

    @Query("SELECT COUNT(c) FROM Category c WHERE c.parentId = :parentId")
    int countChildren(@Param("parentId") Long parentId);

    // ========== Admin Methods (bypass @SQLRestriction) ==========

    /**
     * Find category by ID including deleted (for admin).
     */
    @Query(value = "SELECT * FROM category WHERE id = :id", nativeQuery = true)
    Optional<Category> findByIdIncludingDeleted(@Param("id") Long id);
}
