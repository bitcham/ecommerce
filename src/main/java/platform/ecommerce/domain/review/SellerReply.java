package platform.ecommerce.domain.review;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import platform.ecommerce.domain.common.BaseEntity;
import platform.ecommerce.domain.common.SoftDeletable;
import platform.ecommerce.exception.UnauthorizedReplyException;

import java.time.LocalDateTime;

/**
 * SellerReply aggregate root.
 * Represents a seller's reply to a product review.
 * Separate aggregate from Review for DDD compliance.
 */
@Entity
@Table(name = "seller_reply", indexes = {
        @Index(name = "idx_seller_reply_review", columnList = "review_id"),
        @Index(name = "idx_seller_reply_seller", columnList = "seller_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_seller_reply_review", columnNames = {"review_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerReply extends BaseEntity implements SoftDeletable {

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ========== Factory Method ==========

    /**
     * Create a new seller reply.
     */
    public static SellerReply create(Long reviewId, Long sellerId, String content) {
        validateContent(content);
        SellerReply reply = new SellerReply();
        reply.reviewId = reviewId;
        reply.sellerId = sellerId;
        reply.content = content;
        return reply;
    }

    // ========== Business Methods ==========

    /**
     * Update reply content.
     * Only the owner seller can update.
     */
    public void updateContent(String newContent, Long requestingSellerId) {
        validateOwnership(requestingSellerId);
        validateContent(newContent);
        this.content = newContent;
    }

    /**
     * Check if this reply belongs to the seller.
     */
    public boolean isOwnedBy(Long sellerId) {
        return this.sellerId.equals(sellerId);
    }

    /**
     * Validate ownership before modification.
     */
    public void validateOwnership(Long requestingSellerId) {
        if (!isOwnedBy(requestingSellerId)) {
            throw new UnauthorizedReplyException("Only the reply owner can modify this reply");
        }
    }

    // ========== SoftDeletable Implementation ==========

    @Override
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    @Override
    public void restore() {
        this.deletedAt = null;
    }

    @Override
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    @Override
    public LocalDateTime getDeletedAt() {
        return this.deletedAt;
    }

    // ========== Validation ==========

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Reply content cannot be empty");
        }
    }
}
