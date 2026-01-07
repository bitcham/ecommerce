package platform.ecommerce.domain.review;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * SellerReplyHistory entity.
 * Stores modification history for seller replies (audit trail).
 *
 * <p>History is created ONLY when reply is modified, not on initial creation.
 * This ensures previousContent always has meaningful data.</p>
 *
 * <p>History records are preserved even after reply deletion for audit purposes.</p>
 */
@Entity
@Table(name = "seller_reply_history", indexes = {
        @Index(name = "idx_seller_reply_history_reply", columnList = "seller_reply_id"),
        @Index(name = "idx_seller_reply_history_modified", columnList = "modified_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerReplyHistory extends BaseEntity {

    @Column(name = "seller_reply_id", nullable = false)
    private Long sellerReplyId;

    @Column(name = "previous_content", columnDefinition = "TEXT", nullable = false)
    private String previousContent;

    @Column(name = "modified_by", nullable = false)
    private Long modifiedBy;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    // ========== Factory Method ==========

    /**
     * Create a new history record when reply is modified.
     * Should be called BEFORE updating the reply content.
     *
     * @param sellerReplyId   The ID of the seller reply being modified
     * @param previousContent The content before modification (must not be null/empty)
     * @param modifiedBy      The ID of the seller who made the modification
     * @return new SellerReplyHistory instance
     */
    public static SellerReplyHistory create(Long sellerReplyId, String previousContent, Long modifiedBy) {
        if (previousContent == null || previousContent.isBlank()) {
            throw new IllegalArgumentException("Previous content cannot be empty");
        }

        SellerReplyHistory history = new SellerReplyHistory();
        history.sellerReplyId = sellerReplyId;
        history.previousContent = previousContent;
        history.modifiedBy = modifiedBy;
        history.modifiedAt = LocalDateTime.now();
        return history;
    }
}
