package platform.ecommerce.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errors: List<String>? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message
            )
        }

        fun <T> success(message: String): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message
            )
        }

        fun <T> error(message: String, errors: List<String>? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                errors = errors
            )
        }

        fun <T> error(errors: List<String>): ApiResponse<T> {
            return ApiResponse(
                success = false,
                errors = errors
            )
        }
    }
}
