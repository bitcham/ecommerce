package platform.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import platform.ecommerce.domain.review.Review;
import platform.ecommerce.dto.request.review.ReviewCreateRequest;
import platform.ecommerce.dto.request.review.ReviewUpdateRequest;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.review.RatingSummaryResponse;
import platform.ecommerce.dto.response.review.ReviewResponse;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.InvalidStateException;
import platform.ecommerce.mapper.ReviewMapper;
import platform.ecommerce.repository.review.ReviewRepository;
import platform.ecommerce.service.review.ReviewServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for ReviewService.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review testReview;
    private static final Long MEMBER_ID = 1L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long ORDER_ITEM_ID = 50L;
    private static final Long REVIEW_ID = 10L;

    @BeforeEach
    void setUp() {
        testReview = Review.builder()
                .memberId(MEMBER_ID)
                .productId(PRODUCT_ID)
                .orderItemId(ORDER_ITEM_ID)
                .rating(5)
                .title("Great product!")
                .content("Really satisfied")
                .images(List.of("image.jpg"))
                .build();
        ReflectionTestUtils.setField(testReview, "id", REVIEW_ID);

        // Setup default mapper behaviors
        lenient().when(reviewMapper.toResponse(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            return ReviewResponse.builder()
                    .id(r.getId())
                    .memberId(r.getMemberId())
                    .productId(r.getProductId())
                    .orderItemId(r.getOrderItemId())
                    .rating(r.getRating())
                    .title(r.getTitle())
                    .content(r.getContent())
                    .images(r.getImages())
                    .helpfulCount(r.getHelpfulCount())
                    .verified(r.isVerified())
                    .createdAt(r.getCreatedAt())
                    .updatedAt(r.getUpdatedAt())
                    .build();
        });
    }

    @Nested
    @DisplayName("createReview")
    class CreateReview {

        @Test
        @DisplayName("should create review for purchased product")
        void createReviewSuccessfully() {
            // given
            ReviewCreateRequest request = ReviewCreateRequest.builder()
                    .orderItemId(ORDER_ITEM_ID)
                    .rating(4)
                    .title("Good product")
                    .content("Nice quality")
                    .images(List.of("img.jpg"))
                    .build();

            given(reviewRepository.existsByOrderItemId(ORDER_ITEM_ID)).willReturn(false);
            given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> {
                Review review = invocation.getArgument(0);
                ReflectionTestUtils.setField(review, "id", REVIEW_ID);
                return review;
            });

            // when
            ReviewResponse response = reviewService.createReview(MEMBER_ID, request);

            // then
            assertThat(response.rating()).isEqualTo(4);
            assertThat(response.title()).isEqualTo("Good product");
            assertThat(response.verified()).isTrue();
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("should throw exception for duplicate review")
        void throwOnDuplicateReview() {
            // given
            ReviewCreateRequest request = ReviewCreateRequest.builder()
                    .orderItemId(ORDER_ITEM_ID)
                    .rating(5)
                    .build();

            given(reviewRepository.existsByOrderItemId(ORDER_ITEM_ID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(MEMBER_ID, request))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("getReview")
    class GetReview {

        @Test
        @DisplayName("should return review by id")
        void returnReviewById() {
            // given
            given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(testReview));

            // when
            ReviewResponse response = reviewService.getReview(REVIEW_ID);

            // then
            assertThat(response.id()).isEqualTo(REVIEW_ID);
            assertThat(response.rating()).isEqualTo(5);
        }

        @Test
        @DisplayName("should throw exception when not found")
        void throwOnNotFound() {
            // given
            given(reviewRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.getReview(999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getProductReviews")
    class GetProductReviews {

        @Test
        @DisplayName("should return paginated reviews for product")
        void returnPaginatedReviews() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> reviewPage = new PageImpl<>(List.of(testReview), pageable, 1);
            given(reviewRepository.findByProductId(PRODUCT_ID, pageable)).willReturn(reviewPage);

            // when
            PageResponse<ReviewResponse> response = reviewService.getProductReviews(PRODUCT_ID, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getPage().getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getMyReviews")
    class GetMyReviews {

        @Test
        @DisplayName("should return member's reviews")
        void returnMemberReviews() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> reviewPage = new PageImpl<>(List.of(testReview), pageable, 1);
            given(reviewRepository.findByMemberId(MEMBER_ID, pageable)).willReturn(reviewPage);

            // when
            Page<ReviewResponse> response = reviewService.getMyReviews(MEMBER_ID, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateReview")
    class UpdateReview {

        @Test
        @DisplayName("should update review")
        void updateReviewSuccessfully() {
            // given
            ReviewUpdateRequest request = ReviewUpdateRequest.builder()
                    .rating(3)
                    .title("Updated title")
                    .content("Updated content")
                    .images(List.of("new-image.jpg"))
                    .build();

            given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(testReview));

            // when
            ReviewResponse response = reviewService.updateReview(REVIEW_ID, MEMBER_ID, request);

            // then
            assertThat(response.rating()).isEqualTo(3);
            assertThat(response.title()).isEqualTo("Updated title");
        }

        @Test
        @DisplayName("should throw exception when not owner")
        void throwOnNotOwner() {
            // given
            ReviewUpdateRequest request = ReviewUpdateRequest.builder()
                    .rating(3)
                    .title("Hacked")
                    .build();

            given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(testReview));

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, 999L, request))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("deleteReview")
    class DeleteReview {

        @Test
        @DisplayName("should delete review")
        void deleteReviewSuccessfully() {
            // given
            given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(testReview));

            // when
            reviewService.deleteReview(REVIEW_ID, MEMBER_ID);

            // then
            assertThat(testReview.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("should throw exception when not owner")
        void throwOnNotOwner() {
            // given
            given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(testReview));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(REVIEW_ID, 999L))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("markHelpful")
    class MarkHelpful {

        @Test
        @DisplayName("should increment helpful count")
        void incrementHelpfulCount() {
            // given
            given(reviewRepository.findById(REVIEW_ID)).willReturn(Optional.of(testReview));
            int originalCount = testReview.getHelpfulCount();

            // when
            reviewService.markHelpful(REVIEW_ID);

            // then
            assertThat(testReview.getHelpfulCount()).isEqualTo(originalCount + 1);
        }
    }

    @Nested
    @DisplayName("getProductRatingSummary")
    class GetProductRatingSummary {

        @Test
        @DisplayName("should calculate average rating and distribution")
        void calculateRatingSummary() {
            // given
            given(reviewRepository.getAverageRating(PRODUCT_ID)).willReturn(4.5);
            given(reviewRepository.countByProductId(PRODUCT_ID)).willReturn(10);
            given(reviewRepository.getRatingDistribution(PRODUCT_ID)).willReturn(List.of(
                    new Object[]{5, 6L},
                    new Object[]{4, 3L},
                    new Object[]{3, 1L}
            ));

            // when
            RatingSummaryResponse response = reviewService.getProductRatingSummary(PRODUCT_ID);

            // then
            assertThat(response.averageRating()).isEqualTo(4.5);
            assertThat(response.totalCount()).isEqualTo(10);
            assertThat(response.distribution().get(5)).isEqualTo(6);
            assertThat(response.distribution().get(4)).isEqualTo(3);
            assertThat(response.distribution().get(3)).isEqualTo(1);
            assertThat(response.distribution().get(2)).isZero();
            assertThat(response.distribution().get(1)).isZero();
        }

        @Test
        @DisplayName("should return zero when no reviews")
        void returnZeroWhenNoReviews() {
            // given
            given(reviewRepository.getAverageRating(PRODUCT_ID)).willReturn(null);
            given(reviewRepository.countByProductId(PRODUCT_ID)).willReturn(0);
            given(reviewRepository.getRatingDistribution(PRODUCT_ID)).willReturn(List.of());

            // when
            RatingSummaryResponse response = reviewService.getProductRatingSummary(PRODUCT_ID);

            // then
            assertThat(response.averageRating()).isZero();
            assertThat(response.totalCount()).isZero();
        }
    }
}
