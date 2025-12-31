package platform.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Standard API response wrapper.
 * All API responses should use this format.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorResponse error;
    private final Meta meta;

    @Builder
    private ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.meta = Meta.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorResponse error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorResponse.of(code, message))
                .build();
    }

    @Getter
    public static class Meta {
        private final LocalDateTime timestamp;
        private final String requestId;

        private Meta(LocalDateTime timestamp, String requestId) {
            this.timestamp = timestamp;
            this.requestId = requestId;
        }

        public static Meta now() {
            return new Meta(LocalDateTime.now(), UUID.randomUUID().toString());
        }
    }
}
