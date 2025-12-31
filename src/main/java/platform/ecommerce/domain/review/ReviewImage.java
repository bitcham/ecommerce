package platform.ecommerce.domain.review;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;

/**
 * Review image entity.
 * Stores image information for a review.
 */
@Entity
@Table(name = "review_image_entity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    public ReviewImage(Review review, String imageUrl, int displayOrder) {
        this.review = review;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }

    /**
     * Update display order.
     */
    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * Set review relationship (for JPA).
     */
    void setReview(Review review) {
        this.review = review;
    }
}
