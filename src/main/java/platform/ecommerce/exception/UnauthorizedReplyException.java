package platform.ecommerce.exception;

/**
 * Exception thrown when a user is not authorized to modify a seller reply.
 */
public class UnauthorizedReplyException extends BusinessException {

    public UnauthorizedReplyException() {
        super(ErrorCode.SELLER_REPLY_UNAUTHORIZED);
    }

    public UnauthorizedReplyException(String message) {
        super(ErrorCode.SELLER_REPLY_UNAUTHORIZED, message);
    }

    public static UnauthorizedReplyException notProductOwner(Long reviewId, Long sellerId) {
        return new UnauthorizedReplyException(
                String.format("Seller %d is not the owner of the product for review %d", sellerId, reviewId));
    }

    public static UnauthorizedReplyException notReplyOwner(Long reviewId, Long sellerId) {
        return new UnauthorizedReplyException(
                String.format("Seller %d is not the owner of the reply for review %d", sellerId, reviewId));
    }
}
