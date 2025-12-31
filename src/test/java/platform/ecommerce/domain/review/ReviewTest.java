package platform.ecommerce.domain.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Review aggregate.
 */
class ReviewTest {

    private Review review;
    private static final Long MEMBER_ID = 1L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long ORDER_ITEM_ID = 50L;

    @BeforeEach
    void setUp() {
        review = Review.builder()
                .memberId(MEMBER_ID)
                .productId(PRODUCT_ID)
                .orderItemId(ORDER_ITEM_ID)
                .rating(5)
                .title("Great product!")
                .content("Really satisfied with this purchase.")
                .images(List.of("image1.jpg", "image2.jpg"))
                .build();
        ReflectionTestUtils.setField(review, "id", 1L);
    }

    @Nested
    @DisplayName("Review Creation")
    class ReviewCreation {

        @Test
        @DisplayName("should create review with valid rating")
        void createReviewWithValidRating() {
            // when
            Review newReview = Review.builder()
                    .memberId(MEMBER_ID)
                    .productId(PRODUCT_ID)
                    .orderItemId(ORDER_ITEM_ID)
                    .rating(4)
                    .title("Good product")
                    .content("Nice quality")
                    .build();

            // then
            assertThat(newReview.getRating()).isEqualTo(4);
            assertThat(newReview.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(newReview.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(newReview.getOrderItemId()).isEqualTo(ORDER_ITEM_ID);
        }

        @Test
        @DisplayName("should initialize helpfulCount as 0")
        void initializeHelpfulCountAsZero() {
            // when
            Review newReview = Review.builder()
                    .memberId(MEMBER_ID)
                    .productId(PRODUCT_ID)
                    .orderItemId(ORDER_ITEM_ID)
                    .rating(5)
                    .build();

            // then
            assertThat(newReview.getHelpfulCount()).isZero();
        }

        @Test
        @DisplayName("should initialize as not verified")
        void initializeAsNotVerified() {
            // when
            Review newReview = Review.builder()
                    .memberId(MEMBER_ID)
                    .productId(PRODUCT_ID)
                    .orderItemId(ORDER_ITEM_ID)
                    .rating(5)
                    .build();

            // then
            assertThat(newReview.isVerified()).isFalse();
        }

        @Test
        @DisplayName("should throw exception for rating below 1")
        void throwOnRatingBelowMin() {
            // when & then
            assertThatThrownBy(() -> Review.builder()
                    .memberId(MEMBER_ID)
                    .productId(PRODUCT_ID)
                    .orderItemId(ORDER_ITEM_ID)
                    .rating(0)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("between 1 and 5");
        }

        @Test
        @DisplayName("should throw exception for rating above 5")
        void throwOnRatingAboveMax() {
            // when & then
            assertThatThrownBy(() -> Review.builder()
                    .memberId(MEMBER_ID)
                    .productId(PRODUCT_ID)
                    .orderItemId(ORDER_ITEM_ID)
                    .rating(6)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("between 1 and 5");
        }

        @Test
        @DisplayName("should accept minimum rating of 1")
        void acceptMinRating() {
            // when
            Review newReview = Review.builder()
                    .memberId(MEMBER_ID)
                    .productId(PRODUCT_ID)
                    .orderItemId(ORDER_ITEM_ID)
                    .rating(1)
                    .build();

            // then
            assertThat(newReview.getRating()).isEqualTo(1);
        }

        @Test
        @DisplayName("should accept maximum rating of 5")
        void acceptMaxRating() {
            // when
            Review newReview = Review.builder()
                    .memberId(MEMBER_ID)
                    .productId(PRODUCT_ID)
                    .orderItemId(ORDER_ITEM_ID)
                    .rating(5)
                    .build();

            // then
            assertThat(newReview.getRating()).isEqualTo(5);
        }

        @Test
        @DisplayName("should handle null images")
        void handleNullImages() {
            // when
            Review newReview = Review.builder()
                    .memberId(MEMBER_ID)
                    .productId(PRODUCT_ID)
                    .orderItemId(ORDER_ITEM_ID)
                    .rating(5)
                    .images(null)
                    .build();

            // then
            assertThat(newReview.getImages()).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {

        @Test
        @DisplayName("should update rating and content")
        void updateRatingAndContent() {
            // when
            review.update(3, "Updated title", "Updated content", List.of("new-image.jpg"));

            // then
            assertThat(review.getRating()).isEqualTo(3);
            assertThat(review.getTitle()).isEqualTo("Updated title");
            assertThat(review.getContent()).isEqualTo("Updated content");
            assertThat(review.getImages()).containsExactly("new-image.jpg");
        }

        @Test
        @DisplayName("should throw exception for invalid rating on update")
        void throwOnInvalidRatingUpdate() {
            // when & then
            assertThatThrownBy(() -> review.update(0, "Title", "Content", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should clear images when null provided")
        void clearImagesWhenNull() {
            // when
            review.update(4, "Title", "Content", null);

            // then
            assertThat(review.getImages()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Helpful Count")
    class HelpfulCount {

        @Test
        @DisplayName("should increment helpful count")
        void incrementHelpfulCount() {
            // given
            assertThat(review.getHelpfulCount()).isZero();

            // when
            review.incrementHelpfulCount();
            review.incrementHelpfulCount();

            // then
            assertThat(review.getHelpfulCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Verified Status")
    class VerifiedStatus {

        @Test
        @DisplayName("should mark as verified")
        void markAsVerified() {
            // given
            assertThat(review.isVerified()).isFalse();

            // when
            review.markAsVerified();

            // then
            assertThat(review.isVerified()).isTrue();
        }
    }

    @Nested
    @DisplayName("Ownership")
    class Ownership {

        @Test
        @DisplayName("should return true for owner")
        void returnTrueForOwner() {
            // when & then
            assertThat(review.isOwnedBy(MEMBER_ID)).isTrue();
        }

        @Test
        @DisplayName("should return false for non-owner")
        void returnFalseForNonOwner() {
            // when & then
            assertThat(review.isOwnedBy(999L)).isFalse();
        }
    }

    @Nested
    @DisplayName("Soft Delete")
    class SoftDelete {

        @Test
        @DisplayName("should mark as deleted")
        void markAsDeleted() {
            // when
            review.delete();

            // then
            assertThat(review.isDeleted()).isTrue();
            assertThat(review.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("should restore deleted review")
        void restoreDeletedReview() {
            // given
            review.delete();
            assertThat(review.isDeleted()).isTrue();

            // when
            review.restore();

            // then
            assertThat(review.isDeleted()).isFalse();
            assertThat(review.getDeletedAt()).isNull();
        }
    }
}
