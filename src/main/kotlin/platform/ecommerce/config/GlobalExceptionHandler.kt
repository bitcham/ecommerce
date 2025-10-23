package platform.ecommerce.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import platform.ecommerce.dto.response.ApiResponse
import platform.ecommerce.exception.DuplicateEmailException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmailException(ex: DuplicateEmailException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            message = ex.message ?: "Email already exists"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            message = ex.message ?: "Invalid request"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            message = "Malformed JSON request"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errors = ex.bindingResult.fieldErrors.map { error ->
            "${error.field}: ${error.defaultMessage}"
        }

        val response = ApiResponse.error<Nothing>(
            message = "Validation failed",
            errors = errors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            message = "An unexpected error occurred"
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
