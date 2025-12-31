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
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Review> findByIdNotDeleted(@Param("id") Long id);

    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<Review> findByProductId(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.memberId = :memberId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<Review> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    boolean existsByOrderItemId(Long orderItemId);

    @Query("SELECT r FROM Review r WHERE r.orderItemId = :orderItemId AND r.deletedAt IS NULL")
    Optional<Review> findByOrderItemId(@Param("orderItemId") Long orderItemId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.deletedAt IS NULL")
    Double getAverageRating(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.deletedAt IS NULL")
    int countByProductId(@Param("productId") Long productId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.deletedAt IS NULL GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.verified = true AND r.deletedAt IS NULL")
    int countVerifiedByProductId(@Param("productId") Long productId);
}
