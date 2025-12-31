package platform.ecommerce.exception;

/**
 * Exception for authentication failures.
 */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthenticationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
