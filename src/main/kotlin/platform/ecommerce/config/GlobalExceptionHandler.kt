package platform.ecommerce.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import platform.ecommerce.dto.response.ApiResponse
import platform.ecommerce.exception.*

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmailException(ex: DuplicateEmailException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            message = ex.message ?: "Email already exists"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(ex: InvalidCredentialsException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            message = ex.message ?: "Invalid credentials"
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
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
        val errorMessage = ex.cause?.message ?: ex.message ?: "Invalid request body"

        val message = when {
            errorMessage.contains("missing", ignoreCase = true) ||
            errorMessage.contains("required", ignoreCase = true) ||
            errorMessage.contains("Instantiation", ignoreCase = true) -> {
                "Missing required fields in request body"
            }
            errorMessage.contains("JSON", ignoreCase = true) ||
            errorMessage.contains("parse", ignoreCase = true) -> {
                "Malformed JSON request"
            }
            else -> {
                "Invalid request body"
            }
        }

        val response = ApiResponse.error<Nothing>(message = message)
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

    @ExceptionHandler(TokenNotFoundException::class)
    fun handleTokenNotFoundException(ex: TokenNotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            message = ex.message ?: "Token not found"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(TokenExpiredException::class)
    fun handleTokenExpiredException(ex: TokenExpiredException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            message = ex.message ?: "Token expired"
        )
        return ResponseEntity.status(HttpStatus.GONE).body(response)
    }

    @ExceptionHandler(TokenAlreadyUsedException::class)
    fun handleTokenAlreadyUsedException(ex: TokenAlreadyUsedException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            message = ex.message ?: "Token already used"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(MemberAlreadyActivated::class)
    fun handleMemberAlreadyActivated(ex: MemberAlreadyActivated): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            message = ex.message ?: "Member already activated"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFoundException(ex: ProductNotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.error<Nothing>(
            message = ex.message ?: "Product not found"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }
}
