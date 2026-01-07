package platform.ecommerce.domain.review;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import platform.ecommerce.domain.common.BaseEntity;
import platform.ecommerce.domain.common.SoftDeletable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Review aggregate root.
 * Represents a product review by a member.
 */
@Entity
@Table(name = "review", indexes = {
        @Index(name = "idx_review_product", columnList = "product_id"),
        @Index(name = "idx_review_member", columnList = "member_id")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order_item_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity implements SoftDeletable {

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private int rating;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @CollectionTable(name = "review_image", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ReviewImage> reviewImages = new ArrayList<>();

    @Column(name = "helpful_count", nullable = false)
    private int helpfulCount;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Review(Long memberId, Long productId, Long orderItemId, int rating,
                  String title, String content, List<String> images) {
        validateRating(rating);
        this.memberId = memberId;
        this.productId = productId;
        this.orderItemId = orderItemId;
        this.rating = rating;
        this.title = title;
        this.content = content;
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.helpfulCount = 0;
        this.verified = false;
    }

    /**
     * Update review content.
     */
    public void update(int rating, String title, String content, List<String> images) {
        validateRating(rating);
        this.rating = rating;
        this.title = title;
        this.content = content;
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
    }

    /**
     * Increment helpful count.
     */
    public void incrementHelpfulCount() {
        this.helpfulCount++;
    }

    /**
     * Mark as verified purchase.
     */
    public void markAsVerified() {
        this.verified = true;
    }

    /**
     * Check if this review belongs to the member.
     */
    public boolean isOwnedBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    /**
     * Add an image to the review.
     */
    public ReviewImage addImage(String imageUrl) {
        int nextOrder = reviewImages.size();
        ReviewImage image = ReviewImage.builder()
                .review(this)
                .imageUrl(imageUrl)
                .displayOrder(nextOrder)
                .build();
        reviewImages.add(image);
        return image;
    }

    /**
     * Remove an image from the review.
     */
    public void removeImage(Long imageId) {
        reviewImages.removeIf(img -> img.getId().equals(imageId));
        // Reorder remaining images
        for (int i = 0; i < reviewImages.size(); i++) {
            reviewImages.get(i).updateDisplayOrder(i);
        }
    }

    /**
     * Get review images.
     */
    public List<ReviewImage> getReviewImages() {
        return new ArrayList<>(reviewImages);
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

    private void validateRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException(
                    String.format("Rating must be between %d and %d", MIN_RATING, MAX_RATING));
        }
    }
}
