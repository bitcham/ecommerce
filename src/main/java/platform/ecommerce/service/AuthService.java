package platform.ecommerce.service;

import jakarta.servlet.http.HttpServletRequest;
import platform.ecommerce.dto.request.LoginRequest;
import platform.ecommerce.dto.request.MemberCreateRequest;
import platform.ecommerce.dto.request.TokenRefreshRequest;
import platform.ecommerce.service.auth.EmailNotificationInfo;
import platform.ecommerce.service.auth.LoginResult;
import platform.ecommerce.service.auth.RegistrationResult;
import platform.ecommerce.service.auth.TokenResult;

/**
 * Authentication domain service interface.
 * Returns domain objects/results for ApplicationService to convert to DTOs.
 */
public interface AuthService {

    /**
     * Register a new member and create verification token.
     * Does NOT send email - that's ApplicationService's responsibility.
     *
     * @param request registration request
     * @return registration result containing member and verification token
     */
    RegistrationResult register(MemberCreateRequest request);

    /**
     * Authenticate member and generate tokens.
     *
     * @param request login request
     * @param httpRequest HTTP request for device info
     * @return login result containing member and tokens
     */
    LoginResult login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * Refresh access token using refresh token.
     *
     * @param request token refresh request
     * @return token result with new tokens
     */
    TokenResult refreshToken(TokenRefreshRequest request);

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
     * Create new email verification token.
     * Does NOT send email - that's ApplicationService's responsibility.
     *
     * @param email member email
     * @return email notification info for sending verification email
     */
    EmailNotificationInfo createVerificationToken(String email);

    /**
     * Create password reset token.
     * Does NOT send email - that's ApplicationService's responsibility.
     *
     * @param email member email
     * @return email notification info for sending password reset email
     */
    EmailNotificationInfo createPasswordResetToken(String email);

    /**
     * Reset password using token.
     *
     * @param token password reset token
     * @param newPassword new password
     */
    void resetPassword(String token, String newPassword);
}
