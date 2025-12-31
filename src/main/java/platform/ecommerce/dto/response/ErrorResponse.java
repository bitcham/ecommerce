package platform.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Error response structure.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String code;
    private final String message;
    private final List<FieldError> details;

    @Builder
    private ErrorResponse(String code, String message, List<FieldError> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }

    public static ErrorResponse of(String code, String message, List<FieldError> details) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .details(details)
                .build();
    }

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String value;
        private final String reason;

        public static FieldError of(String field, String value, String reason) {
            return FieldError.builder()
                    .field(field)
                    .value(value)
                    .reason(reason)
                    .build();
        }
    }
}
