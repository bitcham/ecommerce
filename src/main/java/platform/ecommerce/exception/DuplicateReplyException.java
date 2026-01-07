package platform.ecommerce.exception;

/**
 * Exception thrown when attempting to create a duplicate seller reply.
 */
public class DuplicateReplyException extends BusinessException {

    public DuplicateReplyException() {
        super(ErrorCode.SELLER_REPLY_ALREADY_EXISTS);
    }

    public DuplicateReplyException(String message) {
        super(ErrorCode.SELLER_REPLY_ALREADY_EXISTS, message);
    }

    public static DuplicateReplyException forReview(Long reviewId) {
        return new DuplicateReplyException(
                String.format("Seller reply already exists for review: %d", reviewId));
    }
}
