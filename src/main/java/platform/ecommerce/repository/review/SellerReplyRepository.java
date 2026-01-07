package platform.ecommerce.repository.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.review.SellerReply;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SellerReply aggregate root.
 */
public interface SellerReplyRepository extends JpaRepository<SellerReply, Long> {

    /**
     * Find reply by review ID (excluding soft-deleted).
     */
    @Query("SELECT sr FROM SellerReply sr WHERE sr.reviewId = :reviewId AND sr.deletedAt IS NULL")
    Optional<SellerReply> findByReviewIdNotDeleted(@Param("reviewId") Long reviewId);

    /**
     * Check if reply exists for review (excluding soft-deleted).
     */
    boolean existsByReviewIdAndDeletedAtIsNull(Long reviewId);

    /**
     * Find replies by review IDs (for batch loading).
     */
    @Query("SELECT sr FROM SellerReply sr WHERE sr.reviewId IN :reviewIds AND sr.deletedAt IS NULL")
    List<SellerReply> findByReviewIdInNotDeleted(@Param("reviewIds") List<Long> reviewIds);

    /**
     * Find replies by seller ID (excluding soft-deleted).
     */
    @Query("SELECT sr FROM SellerReply sr WHERE sr.sellerId = :sellerId AND sr.deletedAt IS NULL ORDER BY sr.createdAt DESC")
    List<SellerReply> findBySellerIdNotDeleted(@Param("sellerId") Long sellerId);

    /**
     * Count replies by seller ID (excluding soft-deleted).
     */
    @Query("SELECT COUNT(sr) FROM SellerReply sr WHERE sr.sellerId = :sellerId AND sr.deletedAt IS NULL")
    long countBySellerIdNotDeleted(@Param("sellerId") Long sellerId);
}
