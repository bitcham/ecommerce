package platform.ecommerce.repository.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.review.Review;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Review aggregate root.
 * Note: @SQLRestriction on Review entity automatically filters deleted records.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.productId = :productId ORDER BY r.createdAt DESC")
    Page<Review> findByProductId(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.memberId = :memberId ORDER BY r.createdAt DESC")
    Page<Review> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    boolean existsByOrderItemId(Long orderItemId);

    Optional<Review> findByOrderItemId(Long orderItemId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double getAverageRating(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId")
    int countByProductId(@Param("productId") Long productId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = :productId GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.verified = true")
    int countVerifiedByProductId(@Param("productId") Long productId);

    // ========== Admin Methods (bypass @SQLRestriction) ==========

    /**
     * Find review by ID including deleted (for admin).
     */
    @Query(value = "SELECT * FROM review WHERE id = :id", nativeQuery = true)
    Optional<Review> findByIdIncludingDeleted(@Param("id") Long id);
}
