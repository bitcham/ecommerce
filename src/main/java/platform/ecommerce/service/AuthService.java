package platform.ecommerce.service;

import jakarta.servlet.http.HttpServletRequest;
import platform.ecommerce.dto.request.LoginRequest;
import platform.ecommerce.dto.request.MemberCreateRequest;
import platform.ecommerce.dto.request.TokenRefreshRequest;
import platform.ecommerce.dto.response.LoginResponse;
import platform.ecommerce.dto.response.MemberResponse;
import platform.ecommerce.dto.response.TokenResponse;

/**
 * Authentication service interface.
 */
public interface AuthService {

    /**
     * Register a new member and send verification email.
     *
     * @param request registration request
     * @return created member response
     */
    MemberResponse register(MemberCreateRequest request);

    /**
     * Authenticate member and return tokens.
     *
     * @param request login request
     * @param httpRequest HTTP request for device info
     * @return login response with tokens
     */
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * Refresh access token using refresh token.
     *
     * @param request token refresh request
     * @return new token response
     */
    TokenResponse refreshToken(TokenRefreshRequest request);

    /**
     * Logout member and revoke refresh token.
     *
     * @param refreshToken refresh token to revoke
     */
    void logout(String refreshToken);

    /**
     * Logout from all devices by revoking all refresh tokens.
     *
     * @param memberId member ID
     */
    void logoutAll(Long memberId);

    /**
     * Verify email with token.
     *
     * @param token verification token
     */
    void verifyEmail(String token);

    /**
     * Resend verification email.
     *
     * @param email member email
     */
    void resendVerificationEmail(String email);

    /**
     * Request password reset and send reset email.
     *
     * @param email member email
     */
    void requestPasswordReset(String email);

    /**
     * Reset password using token.
     *
     * @param token password reset token
     * @param newPassword new password
     */
    void resetPassword(String token, String newPassword);
}
