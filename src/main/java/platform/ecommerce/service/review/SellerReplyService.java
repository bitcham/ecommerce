package platform.ecommerce.service.review;

import platform.ecommerce.domain.review.SellerReply;
import platform.ecommerce.domain.review.SellerReplyHistory;

import java.util.List;
import java.util.Optional;

/**
 * Domain service for SellerReply aggregate.
 * Returns Entity objects (not DTOs).
 */
public interface SellerReplyService {

    /**
     * Create a new seller reply.
     *
     * @param reviewId Review ID to reply to
     * @param sellerId Seller ID creating the reply
     * @param content  Reply content
     * @return Created SellerReply entity
     */
    SellerReply createReply(Long reviewId, Long sellerId, String content);

    /**
     * Get reply by review ID.
     *
     * @param reviewId Review ID
     * @return SellerReply entity
     * @throws platform.ecommerce.exception.EntityNotFoundException if not found
     */
    SellerReply getReply(Long reviewId);

    /**
     * Get reply by review ID as Optional.
     *
     * @param reviewId Review ID
     * @return Optional containing reply if exists
     */
    Optional<SellerReply> getReplyOptional(Long reviewId);

    /**
     * Update reply content.
     *
     * @param reviewId  Review ID
     * @param sellerId  Seller ID making the update
     * @param newContent New content
     * @return Updated SellerReply entity
     */
    SellerReply updateReply(Long reviewId, Long sellerId, String newContent);

    /**
     * Delete reply (soft delete).
     *
     * @param reviewId Review ID
     * @param sellerId Seller ID requesting deletion
     */
    void deleteReply(Long reviewId, Long sellerId);

    /**
     * Delete reply when review is deleted (event handler use).
     *
     * @param reviewId Review ID
     */
    void deleteByReviewId(Long reviewId);

    /**
     * Get modification history for a reply.
     *
     * @param reviewId Review ID
     * @return List of history records
     */
    List<SellerReplyHistory> getHistory(Long reviewId);
}
