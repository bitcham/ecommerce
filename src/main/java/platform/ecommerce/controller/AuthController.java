package platform.ecommerce.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.dto.request.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.service.application.AuthApplicationService;

/**
 * Authentication REST controller.
 */
@Tag(name = "Authentication", description = "Authentication API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;

    @Operation(summary = "Register", description = "Register a new member account")
    @PostMapping("/register")
    @RateLimiter(name = "register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> register(
            @Valid @RequestBody MemberCreateRequest request
    ) {
        MemberResponse response = authApplicationService.register(request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Login", description = "Authenticate member and get tokens")
    @RateLimiter(name = "auth")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        LoginResponse response = authApplicationService.login(request, httpRequest);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    @RateLimiter(name = "token-action")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        TokenResponse response = authApplicationService.refreshToken(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Logout", description = "Revoke refresh token and logout")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestBody TokenRefreshRequest request
    ) {
        authApplicationService.logout(request.refreshToken());
    }

    @Operation(summary = "Logout all devices", description = "Revoke all refresh tokens for member")
    @PostMapping("/logout-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logoutAll(
            @Parameter(description = "Member ID") @RequestParam Long memberId
    ) {
        authApplicationService.logoutAll(memberId);
    }

    @Operation(summary = "Verify email", description = "Verify email with token")
    @RateLimiter(name = "token-action")
    @GetMapping("/verify-email")
    public ApiResponse<String> verifyEmail(
            @Parameter(description = "Verification token") @RequestParam String token
    ) {
        authApplicationService.verifyEmail(token);
        return ApiResponse.success("Email verified successfully");
    }

    @Operation(summary = "Resend verification email", description = "Resend email verification link")
    @RateLimiter(name = "email-action")
    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendVerificationEmail(
            @Valid @RequestBody EmailVerificationRequest request
    ) {
        authApplicationService.resendVerificationEmail(request.email());
    }

    @Operation(summary = "Request password reset", description = "Request password reset email")
    @RateLimiter(name = "email-action")
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPasswordReset(
            @Valid @RequestBody EmailVerificationRequest request
    ) {
        authApplicationService.requestPasswordReset(request.email());
    }

    @Operation(summary = "Reset password", description = "Reset password with token")
    @RateLimiter(name = "sensitive-action")
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        authApplicationService.resetPassword(request.token(), request.newPassword());
        return ApiResponse.success("Password reset successfully");
    }
}
