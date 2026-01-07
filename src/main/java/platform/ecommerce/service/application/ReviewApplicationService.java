package platform.ecommerce.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import platform.ecommerce.dto.request.review.ReviewCreateRequest;
import platform.ecommerce.dto.request.review.ReviewUpdateRequest;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.review.ReviewImageResponse;
import platform.ecommerce.dto.response.review.ReviewResponse;
import platform.ecommerce.dto.response.review.ReviewStatisticsResponse;
import platform.ecommerce.service.review.ReviewService;

/**
 * Review application service.
 * Currently delegates to ReviewService.
 */
@Service
@RequiredArgsConstructor
public class ReviewApplicationService {

    private final ReviewService reviewService;

    public ReviewResponse createReview(Long memberId, Long productId, ReviewCreateRequest request) {
        return reviewService.createReview(memberId, productId, request);
    }

    public ReviewResponse getReview(Long reviewId) {
        return reviewService.getReview(reviewId);
    }

    public PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        return reviewService.getProductReviews(productId, pageable);
    }

    public PageResponse<ReviewResponse> getMemberReviews(Long memberId, Pageable pageable) {
        return reviewService.getMemberReviews(memberId, pageable);
    }

    public ReviewStatisticsResponse getProductStatistics(Long productId) {
        return reviewService.getProductStatistics(productId);
    }

    public ReviewResponse updateReview(Long reviewId, Long memberId, ReviewUpdateRequest request) {
        return reviewService.updateReview(reviewId, memberId, request);
    }

    public void deleteReview(Long reviewId, Long memberId) {
        reviewService.deleteReview(reviewId, memberId);
    }

    public ReviewImageResponse addImage(Long reviewId, Long memberId, String imageUrl) {
        return reviewService.addImage(reviewId, memberId, imageUrl);
    }

    public void removeImage(Long reviewId, Long imageId, Long memberId) {
        reviewService.removeImage(reviewId, imageId, memberId);
    }
}
