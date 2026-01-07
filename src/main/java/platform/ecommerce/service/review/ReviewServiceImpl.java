package platform.ecommerce.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.review.Review;
import platform.ecommerce.domain.review.ReviewImage;
import platform.ecommerce.dto.request.review.ReviewCreateRequest;
import platform.ecommerce.dto.request.review.ReviewUpdateRequest;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.review.RatingSummaryResponse;
import platform.ecommerce.dto.response.review.ReviewImageResponse;
import platform.ecommerce.dto.response.review.ReviewResponse;
import platform.ecommerce.dto.response.review.ReviewStatisticsResponse;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;
import platform.ecommerce.mapper.ReviewMapper;
import platform.ecommerce.repository.review.ReviewRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Review service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(Long memberId, ReviewCreateRequest request) {
        log.info("Creating review for order item: {} by member: {}", request.orderItemId(), memberId);

        validateNoDuplicateReview(request.orderItemId());

        // In real implementation, we would verify purchase through OrderService
        // For now, we'll create the review with product ID derived from order item
        // This would need integration with OrderService to get productId

        Review review = Review.builder()
                .memberId(memberId)
                .productId(1L) // Placeholder - should come from OrderItem lookup
                .orderItemId(request.orderItemId())
                .rating(request.rating())
                .title(request.title())
                .content(request.content())
                .images(request.images())
                .build();

        // Mark as verified since it's from a valid order item
        review.markAsVerified();

        Review savedReview = reviewRepository.save(review);
        log.info("Review created: id={}", savedReview.getId());

        return reviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse createReview(Long memberId, Long productId, ReviewCreateRequest request) {
        log.info("Creating review for product: {} by member: {}", productId, memberId);

        Review review = Review.builder()
                .memberId(memberId)
                .productId(productId)
                .orderItemId(request.orderItemId())
                .rating(request.rating())
                .title(request.title())
                .content(request.content())
                .images(request.images())
                .build();

        if (request.orderItemId() != null) {
            validateNoDuplicateReview(request.orderItemId());
            review.markAsVerified();
        }

        Review savedReview = reviewRepository.save(review);
        log.info("Review created: id={}", savedReview.getId());

        return reviewMapper.toResponse(savedReview);
    }

    @Override
    public ReviewResponse getReview(Long reviewId) {
        Review review = findReviewById(reviewId);
        return reviewMapper.toResponse(review);
    }

    @Override
    public PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByProductId(productId, pageable);
        return PageResponse.of(page.map(reviewMapper::toResponse));
    }

    @Override
    public PageResponse<ReviewResponse> getMemberReviews(Long memberId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByMemberId(memberId, pageable);
        return PageResponse.of(page.map(reviewMapper::toResponse));
    }

    @Override
    public Page<ReviewResponse> getMyReviews(Long memberId, Pageable pageable) {
        return reviewRepository.findByMemberId(memberId, pageable)
                .map(reviewMapper::toResponse);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long memberId, ReviewUpdateRequest request) {
        log.info("Updating review: id={}", reviewId);

        Review review = findReviewById(reviewId);
        validateOwnership(review, memberId);

        review.update(request.rating(), request.title(), request.content(), request.images());

        log.info("Review updated: id={}", reviewId);
        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long memberId) {
        log.info("Deleting review: id={}", reviewId);

        Review review = findReviewById(reviewId);
        validateOwnership(review, memberId);

        review.delete();

        log.info("Review deleted: id={}", reviewId);
    }

    @Override
    @Transactional
    public void markHelpful(Long reviewId) {
        log.info("Marking review as helpful: id={}", reviewId);

        Review review = findReviewById(reviewId);
        review.incrementHelpfulCount();
    }

    @Override
    public RatingSummaryResponse getProductRatingSummary(Long productId) {
        Double avgRating = reviewRepository.getAverageRating(productId);
        int totalCount = reviewRepository.countByProductId(productId);
        List<Object[]> distributionData = reviewRepository.getRatingDistribution(productId);

        Map<Integer, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0);
        }
        for (Object[] row : distributionData) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(rating, count.intValue());
        }

        return RatingSummaryResponse.builder()
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalCount(totalCount)
                .distribution(distribution)
                .build();
    }

    @Override
    public ReviewStatisticsResponse getProductStatistics(Long productId) {
        Double avgRating = reviewRepository.getAverageRating(productId);
        int totalCount = reviewRepository.countByProductId(productId);
        int verifiedCount = reviewRepository.countVerifiedByProductId(productId);
        List<Object[]> distributionData = reviewRepository.getRatingDistribution(productId);

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        for (Object[] row : distributionData) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(rating, count);
        }

        return ReviewStatisticsResponse.of(
                productId,
                totalCount,
                avgRating != null ? avgRating : 0.0,
                distribution,
                verifiedCount
        );
    }

    @Override
    @Transactional
    public ReviewImageResponse addImage(Long reviewId, Long memberId, String imageUrl) {
        log.info("Adding image to review: reviewId={}", reviewId);

        Review review = findReviewById(reviewId);
        validateOwnership(review, memberId);

        ReviewImage image = review.addImage(imageUrl);

        log.info("Image added to review: reviewId={}, imageId={}", reviewId, image.getId());
        return ReviewImageResponse.from(image);
    }

    @Override
    @Transactional
    public void removeImage(Long reviewId, Long imageId, Long memberId) {
        log.info("Removing image from review: reviewId={}, imageId={}", reviewId, imageId);

        Review review = findReviewById(reviewId);
        validateOwnership(review, memberId);

        review.removeImage(imageId);

        log.info("Image removed from review: reviewId={}, imageId={}", reviewId, imageId);
    }

    // ========== Private Helper Methods ==========

    private Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private void validateNoDuplicateReview(Long orderItemId) {
        if (reviewRepository.existsByOrderItemId(orderItemId)) {
            throw new InvalidStateException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
    }

    private void validateOwnership(Review review, Long memberId) {
        if (!review.isOwnedBy(memberId)) {
            throw new InvalidStateException(ErrorCode.REVIEW_NOT_ALLOWED,
                    "You can only modify your own reviews");
        }
    }
}
