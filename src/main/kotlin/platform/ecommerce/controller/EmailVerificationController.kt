package platform.ecommerce.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import platform.ecommerce.dto.response.ApiResponse
import platform.ecommerce.service.EmailVerificationService
import java.util.*
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerResponse

@Tag(name = "EmailVerification", description = "Email verification")
@RestController
@RequestMapping("/auth")
class EmailVerificationController(
    private val emailVerificationService: EmailVerificationService
) {

    @Operation(summary = "Verify an email", description = "Verifies a member")
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "Successfully verified"),
        SwaggerResponse(responseCode = "410", description = "Token Expired"),
        SwaggerResponse(responseCode = "404", description = "Token Not Found"),
        SwaggerResponse(responseCode = "409", description = "Token Already Used")
    )
    @GetMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    fun verifyEmail(@RequestParam token: UUID): ApiResponse<Unit>{
        emailVerificationService.verifyEmail(token)
        return ApiResponse.success(message = "Successfully verified")
    }
}