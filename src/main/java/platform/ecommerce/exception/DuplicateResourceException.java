package platform.ecommerce.exception;

/**
 * Exception thrown when attempting to create a duplicate resource.
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateResourceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static DuplicateResourceException of(ErrorCode errorCode, String field, String value) {
        return new DuplicateResourceException(errorCode,
            String.format("%s: %s = %s", errorCode.getMessage(), field, value));
    }
}
