package platform.ecommerce.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import platform.ecommerce.dto.response.ApiResponse;
import platform.ecommerce.dto.response.ErrorResponse;
import platform.ecommerce.dto.response.ErrorResponse.FieldError;

import java.util.List;

/**
 * Global exception handler for REST APIs.
 * Catches all exceptions and transforms them into standardized API responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle business exceptions.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {} - {}", e.getErrorCode(), e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse error = ErrorResponse.of(errorCode.name(), e.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(error));
    }

    /**
     * Handle validation exceptions from @Valid.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {
        log.warn("Validation exception: {}", e.getMessage());

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> FieldError.of(
                        error.getField(),
                        error.getRejectedValue() != null ? error.getRejectedValue().toString() : "null",
                        error.getDefaultMessage()))
                .toList();

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.INVALID_INPUT.name(),
                "Validation failed",
                fieldErrors);

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(ApiResponse.error(error));
    }

    /**
     * Handle constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException e) {
        log.warn("Constraint violation: {}", e.getMessage());

        List<FieldError> fieldErrors = e.getConstraintViolations().stream()
                .map(violation -> FieldError.of(
                        violation.getPropertyPath().toString(),
                        violation.getInvalidValue() != null ? violation.getInvalidValue().toString() : "null",
                        violation.getMessage()))
                .toList();

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.INVALID_INPUT.name(),
                "Constraint violation",
                fieldErrors);

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(ApiResponse.error(error));
    }

    /**
     * Handle access denied exceptions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.FORBIDDEN.name(),
                ErrorCode.FORBIDDEN.getMessage());

        return ResponseEntity
                .status(ErrorCode.FORBIDDEN.getHttpStatus())
                .body(ApiResponse.error(error));
    }

    /**
     * Handle method not supported exceptions.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.METHOD_NOT_ALLOWED.name(),
                e.getMessage());

        return ResponseEntity
                .status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(ApiResponse.error(error));
    }

    /**
     * Handle missing parameter exceptions.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameterException(
            MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.INVALID_INPUT.name(),
                String.format("Required parameter '%s' is missing", e.getParameterName()));

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(ApiResponse.error(error));
    }

    /**
     * Handle type mismatch exceptions.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.INVALID_INPUT.name(),
                String.format("Parameter '%s' should be of type '%s'",
                        e.getName(),
                        e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown"));

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(ApiResponse.error(error));
    }

    /**
     * Handle malformed JSON exceptions.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.warn("Message not readable: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.INVALID_INPUT.name(),
                "Malformed JSON request body");

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(ApiResponse.error(error));
    }

    /**
     * Handle resource not found exceptions.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
            NoResourceFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.RESOURCE_NOT_FOUND.name(),
                "Resource not found: " + e.getResourcePath());

        return ResponseEntity
                .status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus())
                .body(ApiResponse.error(error));
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected error occurred", e);

        ErrorResponse error = ErrorResponse.of(
                ErrorCode.INTERNAL_ERROR.name(),
                ErrorCode.INTERNAL_ERROR.getMessage());

        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ApiResponse.error(error));
    }
}
