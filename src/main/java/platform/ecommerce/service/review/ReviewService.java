package platform.ecommerce.service.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import platform.ecommerce.dto.request.review.ReviewCreateRequest;
import platform.ecommerce.dto.request.review.ReviewUpdateRequest;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.review.RatingSummaryResponse;
import platform.ecommerce.dto.response.review.ReviewImageResponse;
import platform.ecommerce.dto.response.review.ReviewResponse;
import platform.ecommerce.dto.response.review.ReviewStatisticsResponse;

/**
 * Service interface for Review operations.
 */
public interface ReviewService {

    /**
     * Creates a new review.
     */
    ReviewResponse createReview(Long memberId, ReviewCreateRequest request);

    /**
     * Creates a new review for a product.
     */
    ReviewResponse createReview(Long memberId, Long productId, ReviewCreateRequest request);

    /**
     * Gets a review by ID.
     */
    ReviewResponse getReview(Long reviewId);

    /**
     * Gets reviews for a product with PageResponse.
     */
    PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable);

    /**
     * Gets reviews by a member with PageResponse.
     */
    PageResponse<ReviewResponse> getMemberReviews(Long memberId, Pageable pageable);

    /**
     * Gets reviews by a member.
     */
    Page<ReviewResponse> getMyReviews(Long memberId, Pageable pageable);

    /**
     * Updates a review.
     */
    ReviewResponse updateReview(Long reviewId, Long memberId, ReviewUpdateRequest request);

    /**
     * Deletes a review.
     */
    void deleteReview(Long reviewId, Long memberId);

    /**
     * Marks a review as helpful.
     */
    void markHelpful(Long reviewId);

    /**
     * Gets rating summary for a product.
     */
    RatingSummaryResponse getProductRatingSummary(Long productId);

    /**
     * Gets review statistics for a product.
     */
    ReviewStatisticsResponse getProductStatistics(Long productId);

    /**
     * Adds an image to a review.
     */
    ReviewImageResponse addImage(Long reviewId, Long memberId, String imageUrl);

    /**
     * Removes an image from a review.
     */
    void removeImage(Long reviewId, Long imageId, Long memberId);
}
