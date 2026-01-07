package platform.ecommerce.repository.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.review.SellerReply;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SellerReply aggregate root.
 * Note: @SQLRestriction on SellerReply entity automatically filters deleted records.
 */
public interface SellerReplyRepository extends JpaRepository<SellerReply, Long> {

    /**
     * Find reply by review ID.
     */
    Optional<SellerReply> findByReviewId(Long reviewId);

    /**
     * Check if reply exists for review.
     */
    boolean existsByReviewId(Long reviewId);

    /**
     * Find replies by review IDs (for batch loading).
     */
    @Query("SELECT sr FROM SellerReply sr WHERE sr.reviewId IN :reviewIds")
    List<SellerReply> findByReviewIdIn(@Param("reviewIds") List<Long> reviewIds);

    /**
     * Find replies by seller ID.
     */
    @Query("SELECT sr FROM SellerReply sr WHERE sr.sellerId = :sellerId ORDER BY sr.createdAt DESC")
    List<SellerReply> findBySellerId(@Param("sellerId") Long sellerId);

    /**
     * Count replies by seller ID.
     */
    @Query("SELECT COUNT(sr) FROM SellerReply sr WHERE sr.sellerId = :sellerId")
    long countBySellerId(@Param("sellerId") Long sellerId);

    // ========== Admin Methods (bypass @SQLRestriction) ==========

    /**
     * Find seller reply by ID including deleted (for admin).
     */
    @Query(value = "SELECT * FROM seller_reply WHERE id = :id", nativeQuery = true)
    Optional<SellerReply> findByIdIncludingDeleted(@Param("id") Long id);
}
