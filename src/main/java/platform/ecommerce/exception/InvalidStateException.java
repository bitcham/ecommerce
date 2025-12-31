package platform.ecommerce.exception;

/**
 * Exception thrown when an operation is not allowed due to invalid state.
 */
public class InvalidStateException extends BusinessException {

    public InvalidStateException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidStateException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
