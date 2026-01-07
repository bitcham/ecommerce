package platform.ecommerce.repository.review;

import org.springframework.data.jpa.repository.JpaRepository;
import platform.ecommerce.domain.review.SellerReplyHistory;

import java.util.List;

/**
 * Repository for SellerReplyHistory entity.
 * Note: No soft delete - history records are preserved permanently.
 */
public interface SellerReplyHistoryRepository extends JpaRepository<SellerReplyHistory, Long> {

    /**
     * Find all history records for a reply, ordered by modification time descending.
     */
    List<SellerReplyHistory> findBySellerReplyIdOrderByModifiedAtDesc(Long sellerReplyId);

    /**
     * Count history records for a reply.
     */
    long countBySellerReplyId(Long sellerReplyId);
}
