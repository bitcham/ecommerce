package platform.ecommerce.exception;

/**
 * Exception thrown when an entity is not found.
 */
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EntityNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static EntityNotFoundException of(ErrorCode errorCode, Long id) {
        return new EntityNotFoundException(errorCode,
            String.format("%s with id %d", errorCode.getMessage(), id));
    }

    public static EntityNotFoundException of(ErrorCode errorCode, String identifier) {
        return new EntityNotFoundException(errorCode,
            String.format("%s: %s", errorCode.getMessage(), identifier));
    }
}
