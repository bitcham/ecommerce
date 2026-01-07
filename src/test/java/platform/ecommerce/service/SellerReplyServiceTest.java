package platform.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import platform.ecommerce.domain.product.Product;
import platform.ecommerce.domain.review.Review;
import platform.ecommerce.domain.review.SellerReply;
import platform.ecommerce.domain.review.SellerReplyHistory;
import platform.ecommerce.exception.DuplicateReplyException;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.UnauthorizedReplyException;
import platform.ecommerce.repository.product.ProductRepository;
import platform.ecommerce.repository.review.ReviewRepository;
import platform.ecommerce.repository.review.SellerReplyHistoryRepository;
import platform.ecommerce.repository.review.SellerReplyRepository;
import platform.ecommerce.service.review.SellerReplyServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for SellerReplyService (Domain Service).
 * Domain Service returns Entity, not DTO.
 */
@ExtendWith(MockitoExtension.class)
class SellerReplyServiceTest {

    @Mock
    private SellerReplyRepository replyRepository;

    @Mock
    private SellerReplyHistoryRepository historyRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SellerReplyServiceImpl sellerReplyService;

    private Review testReview;
    private Product testProduct;
    private SellerReply testReply;

    private static final Long REVIEW_ID = 10L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long SELLER_ID = 5L;
    private static final Long OTHER_SELLER_ID = 99L;
    private static final Long REPLY_ID = 1L;
    private static final String CONTENT = "감사합니다. 좋은 리뷰 감사합니다!";

    @BeforeEach
    void setUp() {
        // Setup test review
        testReview = Review.builder()
                .memberId(1L)
                .productId(PRODUCT_ID)
                .orderItemId(50L)
                .rating(5)
                .title("Great product!")
                .content("Really satisfied")
                .build();
        ReflectionTestUtils.setField(testReview, "id", REVIEW_ID);

        // Setup test product with seller
        testProduct = Product.builder()
                .name("Test Product")
                .sellerId(SELLER_ID)
                .build();
        ReflectionTestUtils.setField(testProduct, "id", PRODUCT_ID);

        // Setup test reply
        testReply = SellerReply.create(REVIEW_ID, SELLER_ID, CONTENT);
        ReflectionTestUtils.setField(testReply, "id", REPLY_ID);
    }

    // ========================================
    // CREATE REPLY
    // ========================================
    @Nested
    @DisplayName("createReply")
    class CreateReply {

        @Test
        @DisplayName("should create reply and return Entity")
        void createReplySuccessfully() {
            // given
            given(reviewRepository.findByIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReview));
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(testProduct));
            given(replyRepository.existsByReviewIdAndDeletedAtIsNull(REVIEW_ID)).willReturn(false);
            given(replyRepository.save(any(SellerReply.class))).willAnswer(invocation -> {
                SellerReply reply = invocation.getArgument(0);
                ReflectionTestUtils.setField(reply, "id", REPLY_ID);
                return reply;
            });

            // when
            SellerReply result = sellerReplyService.createReply(REVIEW_ID, SELLER_ID, CONTENT);

            // then
            assertThat(result).isInstanceOf(SellerReply.class);
            assertThat(result.getReviewId()).isEqualTo(REVIEW_ID);
            assertThat(result.getSellerId()).isEqualTo(SELLER_ID);
            assertThat(result.getContent()).isEqualTo(CONTENT);
            verify(replyRepository).save(any(SellerReply.class));
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when review not found")
        void throwOnReviewNotFound() {
            // given
            given(reviewRepository.findByIdNotDeleted(REVIEW_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sellerReplyService.createReply(REVIEW_ID, SELLER_ID, CONTENT))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Review");
        }

        @Test
        @DisplayName("should throw UnauthorizedReplyException when not product owner")
        void throwOnNotProductOwner() {
            // given
            given(reviewRepository.findByIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReview));
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(testProduct));

            // when & then
            assertThatThrownBy(() -> sellerReplyService.createReply(REVIEW_ID, OTHER_SELLER_ID, CONTENT))
                    .isInstanceOf(UnauthorizedReplyException.class);
        }

        @Test
        @DisplayName("should throw DuplicateReplyException when reply already exists")
        void throwOnDuplicateReply() {
            // given
            given(reviewRepository.findByIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReview));
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(testProduct));
            given(replyRepository.existsByReviewIdAndDeletedAtIsNull(REVIEW_ID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerReplyService.createReply(REVIEW_ID, SELLER_ID, CONTENT))
                    .isInstanceOf(DuplicateReplyException.class);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when content is empty")
        void throwOnEmptyContent() {
            // given
            given(reviewRepository.findByIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReview));
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(testProduct));
            given(replyRepository.existsByReviewIdAndDeletedAtIsNull(REVIEW_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> sellerReplyService.createReply(REVIEW_ID, SELLER_ID, ""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ========================================
    // GET REPLY
    // ========================================
    @Nested
    @DisplayName("getReply")
    class GetReply {

        @Test
        @DisplayName("should return reply Entity when exists")
        void returnReplyWhenExists() {
            // given
            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReply));

            // when
            SellerReply result = sellerReplyService.getReply(REVIEW_ID);

            // then
            assertThat(result).isInstanceOf(SellerReply.class);
            assertThat(result.getReviewId()).isEqualTo(REVIEW_ID);
            assertThat(result.getContent()).isEqualTo(CONTENT);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when not found")
        void throwOnNotFound() {
            // given
            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sellerReplyService.getReply(REVIEW_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("SellerReply");
        }
    }

    @Nested
    @DisplayName("getReplyOptional")
    class GetReplyOptional {

        @Test
        @DisplayName("should return Optional with reply when exists")
        void returnOptionalWithReply() {
            // given
            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReply));

            // when
            Optional<SellerReply> result = sellerReplyService.getReplyOptional(REVIEW_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getContent()).isEqualTo(CONTENT);
        }

        @Test
        @DisplayName("should return empty Optional when not exists")
        void returnEmptyOptional() {
            // given
            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.empty());

            // when
            Optional<SellerReply> result = sellerReplyService.getReplyOptional(REVIEW_ID);

            // then
            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // UPDATE REPLY
    // ========================================
    @Nested
    @DisplayName("updateReply")
    class UpdateReply {

        private static final String NEW_CONTENT = "수정된 답변입니다.";

        @Test
        @DisplayName("should update reply and return Entity")
        void updateReplySuccessfully() {
            // given
            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReply));

            // when
            SellerReply result = sellerReplyService.updateReply(REVIEW_ID, SELLER_ID, NEW_CONTENT);

            // then
            assertThat(result).isInstanceOf(SellerReply.class);
            assertThat(result.getContent()).isEqualTo(NEW_CONTENT);
        }

        @Test
        @DisplayName("should throw UnauthorizedReplyException when not owner")
        void throwOnNotOwner() {
            // given
            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReply));

            // when & then
            assertThatThrownBy(() -> sellerReplyService.updateReply(REVIEW_ID, OTHER_SELLER_ID, NEW_CONTENT))
                    .isInstanceOf(UnauthorizedReplyException.class);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when reply not found")
        void throwOnReplyNotFound() {
            // given
            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sellerReplyService.updateReply(REVIEW_ID, SELLER_ID, NEW_CONTENT))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ========================================
    // DELETE REPLY
    // ========================================
    @Nested
    @DisplayName("deleteReply")
    class DeleteReply {

        @Test
        @DisplayName("should soft delete reply")
        void deleteReplySuccessfully() {
            // given
            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReply));

            // when
            sellerReplyService.deleteReply(REVIEW_ID, SELLER_ID);

            // then
            assertThat(testReply.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("should throw UnauthorizedReplyException when not owner")
        void throwOnNotOwner() {
            // given
            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReply));

            // when & then
            assertThatThrownBy(() -> sellerReplyService.deleteReply(REVIEW_ID, OTHER_SELLER_ID))
                    .isInstanceOf(UnauthorizedReplyException.class);
        }
    }

    // ========================================
    // GET HISTORY
    // ========================================
    @Nested
    @DisplayName("getHistory")
    class GetHistory {

        @Test
        @DisplayName("should return history list")
        void returnHistoryList() {
            // given
            SellerReplyHistory history1 = SellerReplyHistory.create(REPLY_ID, "첫 번째 내용", SELLER_ID);
            SellerReplyHistory history2 = SellerReplyHistory.create(REPLY_ID, "두 번째 내용", SELLER_ID);

            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReply));
            given(historyRepository.findBySellerReplyIdOrderByModifiedAtDesc(REPLY_ID))
                    .willReturn(List.of(history2, history1));

            // when
            List<SellerReplyHistory> result = sellerReplyService.getHistory(REVIEW_ID);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPreviousContent()).isEqualTo("두 번째 내용");
        }

        @Test
        @DisplayName("should return empty list when no history")
        void returnEmptyListWhenNoHistory() {
            // given
            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReply));
            given(historyRepository.findBySellerReplyIdOrderByModifiedAtDesc(REPLY_ID))
                    .willReturn(List.of());

            // when
            List<SellerReplyHistory> result = sellerReplyService.getHistory(REVIEW_ID);

            // then
            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // DELETE BY REVIEW (Event Handler용)
    // ========================================
    @Nested
    @DisplayName("deleteByReviewId")
    class DeleteByReviewId {

        @Test
        @DisplayName("should soft delete reply when review is deleted")
        void deleteReplyWhenReviewDeleted() {
            // given
            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.of(testReply));

            // when
            sellerReplyService.deleteByReviewId(REVIEW_ID);

            // then
            assertThat(testReply.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("should do nothing when no reply exists")
        void doNothingWhenNoReply() {
            // given
            given(replyRepository.findByReviewIdNotDeleted(REVIEW_ID)).willReturn(Optional.empty());

            // when & then (no exception)
            assertThatCode(() -> sellerReplyService.deleteByReviewId(REVIEW_ID))
                    .doesNotThrowAnyException();
        }
    }
}
