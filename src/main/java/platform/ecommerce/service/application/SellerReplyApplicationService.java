package platform.ecommerce.service.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.product.Product;
import platform.ecommerce.domain.review.Review;
import platform.ecommerce.domain.review.SellerReply;
import platform.ecommerce.domain.review.SellerReplyHistory;
import platform.ecommerce.dto.request.SellerReplyRequest;
import platform.ecommerce.dto.response.SellerReplyHistoryResponse;
import platform.ecommerce.dto.response.SellerReplyResponse;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.UnauthorizedReplyException;
import platform.ecommerce.mapper.SellerReplyMapper;
import platform.ecommerce.repository.product.ProductRepository;
import platform.ecommerce.repository.review.ReviewRepository;
import platform.ecommerce.repository.review.SellerReplyHistoryRepository;
import platform.ecommerce.service.review.SellerReplyService;

import java.util.List;
import java.util.Optional;

/**
 * Application service for SellerReply.
 * Handles DTO conversion, orchestration, history management, and authorization.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerReplyApplicationService {

    private final SellerReplyService sellerReplyService;
    private final SellerReplyHistoryRepository historyRepository;
    private final SellerReplyMapper mapper;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    /**
     * Create a new seller reply.
     */
    @Transactional
    public SellerReplyResponse createReply(Long reviewId, Long sellerId, SellerReplyRequest request) {
        SellerReply reply = sellerReplyService.createReply(reviewId, sellerId, request.content());
        return mapper.toResponse(reply, false);  // New reply, not edited
    }

    /**
     * Get reply by review ID (public - anyone can view).
     */
    public SellerReplyResponse getReply(Long reviewId) {
        SellerReply reply = sellerReplyService.getReply(reviewId);
        boolean isEdited = historyRepository.countBySellerReplyId(reply.getId()) > 0;
        return mapper.toResponse(reply, isEdited);
    }

    /**
     * Get reply by review ID as Optional (public).
     */
    public Optional<SellerReplyResponse> getReplyOptional(Long reviewId) {
        return sellerReplyService.getReplyOptional(reviewId)
                .map(reply -> {
                    boolean isEdited = historyRepository.countBySellerReplyId(reply.getId()) > 0;
                    return mapper.toResponse(reply, isEdited);
                });
    }

    /**
     * Update reply content.
     * Saves previous content to history before updating.
     */
    @Transactional
    public SellerReplyResponse updateReply(Long reviewId, Long sellerId, SellerReplyRequest request) {
        // 1. Get existing reply
        SellerReply existingReply = sellerReplyService.getReply(reviewId);
        String previousContent = existingReply.getContent();

        // 2. Skip if content is the same
        if (previousContent.equals(request.content())) {
            boolean isEdited = historyRepository.countBySellerReplyId(existingReply.getId()) > 0;
            return mapper.toResponse(existingReply, isEdited);
        }

        // 3. Save history BEFORE updating
        SellerReplyHistory history = SellerReplyHistory.create(
                existingReply.getId(),
                previousContent,
                sellerId
        );
        historyRepository.save(history);

        // 4. Update reply via domain service
        SellerReply updatedReply = sellerReplyService.updateReply(reviewId, sellerId, request.content());

        log.info("Seller reply updated with history: reviewId={}, historyId={}", reviewId, history.getId());
        return mapper.toResponse(updatedReply, true);  // Now edited
    }

    /**
     * Delete reply (soft delete).
     */
    @Transactional
    public void deleteReply(Long reviewId, Long sellerId) {
        sellerReplyService.deleteReply(reviewId, sellerId);
    }

    /**
     * Get modification history (for product owner SELLER only).
     * Validates that the requesting seller owns the product.
     */
    public List<SellerReplyHistoryResponse> getHistoryBySeller(Long reviewId, Long sellerId) {
        // Validate seller is the product owner
        validateProductOwnership(reviewId, sellerId);

        List<SellerReplyHistory> histories = sellerReplyService.getHistory(reviewId);
        return histories.stream()
                .map(mapper::toHistoryResponse)
                .toList();
    }

    /**
     * Get modification history (for ADMIN - no ownership check).
     */
    public List<SellerReplyHistoryResponse> getHistoryByAdmin(Long reviewId) {
        List<SellerReplyHistory> histories = sellerReplyService.getHistory(reviewId);
        return histories.stream()
                .map(mapper::toHistoryResponse)
                .toList();
    }

    // ========== Private Helper Methods ==========

    private void validateProductOwnership(Long reviewId, Long sellerId) {
        Review review = reviewRepository.findByIdNotDeleted(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.REVIEW_NOT_FOUND,
                        String.format("Review not found: %d", reviewId)));

        Product product = productRepository.findById(review.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        String.format("Product not found: %d", review.getProductId())));

        if (!product.getSellerId().equals(sellerId)) {
            throw UnauthorizedReplyException.notProductOwner(reviewId, sellerId);
        }
    }
}
