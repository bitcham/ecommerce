package platform.ecommerce.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import platform.ecommerce.dto.request.LoginRequest
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.dto.response.ApiResponse
import platform.ecommerce.dto.response.LoginResponse
import platform.ecommerce.dto.response.MemberResponse
import platform.ecommerce.service.AuthService

@Tag(name = "Authentication", description = "Member authentication and registration")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {


    @Operation(summary = "Register a new member", description = "Creates a new member with PENDING status")
    @ApiResponses(
        SwaggerResponse(responseCode = "201", description = "Successfully registered"),
        SwaggerResponse(responseCode = "409", description = "Email already exists"),
        SwaggerResponse(responseCode = "400", description = "Invalid input")
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: MemberRegister): ApiResponse<MemberResponse> {
        val registered = authService.register(request)
        return ApiResponse.success(registered, "Member registered successfully")
    }

    @Operation(
        summary = "Login a member",
        description = "Authenticates a member and returns JWT tokens. Member must be in ACTIVE "
                + "status.")
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "Successfully logged in"),
        SwaggerResponse(responseCode = "400", description = "Invalid input")
    )
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<LoginResponse> {
        return ApiResponse.success(authService.login(request), "Successfully logged in")
    }
}