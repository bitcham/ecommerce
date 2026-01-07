package platform.ecommerce.service.application;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import platform.ecommerce.dto.request.LoginRequest;
import platform.ecommerce.dto.request.MemberCreateRequest;
import platform.ecommerce.dto.request.TokenRefreshRequest;
import platform.ecommerce.dto.response.LoginResponse;
import platform.ecommerce.dto.response.MemberResponse;
import platform.ecommerce.dto.response.TokenResponse;
import platform.ecommerce.mapper.AuthMapper;
import platform.ecommerce.mapper.MemberMapper;
import platform.ecommerce.service.AuthService;
import platform.ecommerce.service.auth.EmailNotificationInfo;
import platform.ecommerce.service.auth.LoginResult;
import platform.ecommerce.service.auth.RegistrationResult;
import platform.ecommerce.service.auth.TokenResult;
import platform.ecommerce.service.email.EmailService;

/**
 * Authentication application service.
 * Handles DTO conversion and orchestrates side effects (email sending).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final AuthService authService;
    private final AuthMapper authMapper;
    private final MemberMapper memberMapper;
    private final EmailService emailService;

    /**
     * Register a new member and send verification email.
     */
    public MemberResponse register(MemberCreateRequest request) {
        RegistrationResult result = authService.register(request);

        // Send verification email (side effect)
        emailService.sendVerificationEmail(
                result.member().getEmail(),
                result.member().getName(),
                result.verificationToken()
        );
        log.info("Verification email sent to: {}", result.member().getEmail());

        return memberMapper.toResponse(result.member());
    }

    /**
     * Authenticate member and return login response with tokens.
     */
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        LoginResult result = authService.login(request, httpRequest);
        return authMapper.toLoginResponse(result, memberMapper);
    }

    /**
     * Refresh access token using refresh token.
     */
    public TokenResponse refreshToken(TokenRefreshRequest request) {
        TokenResult result = authService.refreshToken(request);
        return authMapper.toTokenResponse(result);
    }

    /**
     * Logout and revoke refresh token.
     */
    public void logout(String refreshToken) {
        authService.logout(refreshToken);
    }

    /**
     * Logout from all devices.
     */
    public void logoutAll(Long memberId) {
        authService.logoutAll(memberId);
    }

    /**
     * Verify email with token.
     */
    public void verifyEmail(String token) {
        authService.verifyEmail(token);
    }

    /**
     * Resend verification email.
     */
    public void resendVerificationEmail(String email) {
        EmailNotificationInfo info = authService.createVerificationToken(email);

        // Send verification email (side effect)
        emailService.sendVerificationEmail(
                info.email(),
                info.name(),
                info.token()
        );
        log.info("Verification email resent to: {}", info.email());
    }

    /**
     * Request password reset and send email.
     */
    public void requestPasswordReset(String email) {
        EmailNotificationInfo info = authService.createPasswordResetToken(email);

        // Send password reset email (side effect)
        emailService.sendPasswordResetEmail(
                info.email(),
                info.name(),
                info.token()
        );
        log.info("Password reset email sent to: {}", info.email());
    }

    /**
     * Reset password with token.
     */
    public void resetPassword(String token, String newPassword) {
        authService.resetPassword(token, newPassword);
    }
}
