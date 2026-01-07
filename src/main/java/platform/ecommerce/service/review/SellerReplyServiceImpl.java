package platform.ecommerce.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.product.Product;
import platform.ecommerce.domain.review.Review;
import platform.ecommerce.domain.review.SellerReply;
import platform.ecommerce.domain.review.SellerReplyHistory;
import platform.ecommerce.exception.DuplicateReplyException;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.UnauthorizedReplyException;
import platform.ecommerce.repository.product.ProductRepository;
import platform.ecommerce.repository.review.ReviewRepository;
import platform.ecommerce.repository.review.SellerReplyHistoryRepository;
import platform.ecommerce.repository.review.SellerReplyRepository;

import java.util.List;
import java.util.Optional;

/**
 * Domain service implementation for SellerReply aggregate.
 * Handles business logic and returns Entity objects.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerReplyServiceImpl implements SellerReplyService {

    private final SellerReplyRepository replyRepository;
    private final SellerReplyHistoryRepository historyRepository;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public SellerReply createReply(Long reviewId, Long sellerId, String content) {
        // 1. Validate review exists
        Review review = findReviewById(reviewId);

        // 2. Validate seller is the product owner
        validateProductOwnership(review.getProductId(), sellerId, reviewId);

        // 3. Check for duplicate reply
        if (replyRepository.existsByReviewId(reviewId)) {
            throw DuplicateReplyException.forReview(reviewId);
        }

        // 4. Create and save reply
        SellerReply reply = SellerReply.create(reviewId, sellerId, content);
        SellerReply savedReply = replyRepository.save(reply);

        log.info("Seller reply created: reviewId={}, sellerId={}", reviewId, sellerId);
        return savedReply;
    }

    @Override
    public SellerReply getReply(Long reviewId) {
        return replyRepository.findByReviewId(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.SELLER_REPLY_NOT_FOUND,
                        String.format("SellerReply not found for review: %d", reviewId)));
    }

    @Override
    public Optional<SellerReply> getReplyOptional(Long reviewId) {
        return replyRepository.findByReviewId(reviewId);
    }

    @Override
    @Transactional
    public SellerReply updateReply(Long reviewId, Long sellerId, String newContent) {
        // 1. Find existing reply
        SellerReply reply = getReply(reviewId);

        // 2. Update content (entity method handles ownership validation)
        reply.updateContent(newContent, sellerId);

        log.info("Seller reply updated: reviewId={}, sellerId={}", reviewId, sellerId);
        return reply;
    }

    @Override
    @Transactional
    public void deleteReply(Long reviewId, Long sellerId) {
        SellerReply reply = getReply(reviewId);
        reply.validateOwnership(sellerId);
        reply.delete();

        log.info("Seller reply deleted: reviewId={}, sellerId={}", reviewId, sellerId);
    }

    @Override
    @Transactional
    public void deleteByReviewId(Long reviewId) {
        replyRepository.findByReviewId(reviewId)
                .ifPresent(reply -> {
                    reply.delete();
                    log.info("Seller reply deleted due to review deletion: reviewId={}", reviewId);
                });
    }

    @Override
    public List<SellerReplyHistory> getHistory(Long reviewId) {
        SellerReply reply = getReply(reviewId);
        return historyRepository.findBySellerReplyIdOrderByModifiedAtDesc(reply.getId());
    }

    // ========== Private Helper Methods ==========

    private Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.REVIEW_NOT_FOUND,
                        String.format("Review not found: %d", reviewId)));
    }

    private void validateProductOwnership(Long productId, Long sellerId, Long reviewId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        String.format("Product not found: %d", productId)));

        if (!product.getSellerId().equals(sellerId)) {
            throw UnauthorizedReplyException.notProductOwner(reviewId, sellerId);
        }
    }
}
