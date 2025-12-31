package platform.ecommerce.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.auth.EmailVerificationToken;
import platform.ecommerce.domain.auth.PasswordResetToken;
import platform.ecommerce.domain.auth.RefreshToken;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.domain.member.MemberStatus;
import platform.ecommerce.dto.request.LoginRequest;
import platform.ecommerce.dto.request.MemberCreateRequest;
import platform.ecommerce.dto.request.TokenRefreshRequest;
import platform.ecommerce.dto.response.LoginResponse;
import platform.ecommerce.dto.response.MemberResponse;
import platform.ecommerce.dto.response.TokenResponse;
import platform.ecommerce.exception.*;
import platform.ecommerce.mapper.MemberMapper;
import platform.ecommerce.repository.EmailVerificationTokenRepository;
import platform.ecommerce.repository.MemberRepository;
import platform.ecommerce.repository.PasswordResetTokenRepository;
import platform.ecommerce.repository.RefreshTokenRepository;
import platform.ecommerce.security.JwtTokenProvider;
import platform.ecommerce.service.email.EmailService;

import java.time.LocalDateTime;

/**
 * Authentication service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberMapper memberMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public MemberResponse register(MemberCreateRequest request) {
        log.info("Registering new member with email: {}", request.email());

        validatePasswordMatch(request);
        validateEmailNotExists(request.email());

        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .build();

        Member savedMember = memberRepository.save(member);

        // Create email verification token
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .member(savedMember)
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        // Send verification email asynchronously
        emailService.sendVerificationEmail(
                savedMember.getEmail(),
                savedMember.getName(),
                verificationToken.getToken()
        );
        log.info("Member registered successfully: id={}, verification email sent", savedMember.getId());

        return memberMapper.toResponse(savedMember);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Login attempt for email: {}", request.email());

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationException(ErrorCode.INVALID_CREDENTIALS));

        validatePassword(member, request.password());
        validateMemberStatus(member);

        // Update last login
        member.updateLastLogin();

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(member);
        RefreshToken refreshToken = createRefreshToken(member, httpRequest);

        log.info("Login successful for member: id={}", member.getId());

        TokenResponse tokenResponse = TokenResponse.of(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getAccessTokenExpirationSeconds()
        );

        return LoginResponse.of(tokenResponse, memberMapper.toResponse(member));
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(TokenRefreshRequest request) {
        log.info("Token refresh request");

        RefreshToken refreshToken = refreshTokenRepository
                .findValidToken(request.refreshToken(), LocalDateTime.now())
                .orElseThrow(() -> new AuthenticationException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        Member member = refreshToken.getMember();
        validateMemberStatus(member);

        // Rotate refresh token
        String newRefreshTokenValue = refreshToken.rotate(jwtTokenProvider.getRefreshTokenExpirationDays());

        // Generate new access token
        String accessToken = jwtTokenProvider.generateAccessToken(member);

        log.info("Token refreshed for member: id={}", member.getId());

        return TokenResponse.of(accessToken, newRefreshTokenValue, jwtTokenProvider.getAccessTokenExpirationSeconds());
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Logout request");

        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.revoke();
                    log.info("Refresh token revoked for member: id={}", token.getMember().getId());
                });
    }

    @Override
    @Transactional
    public void logoutAll(Long memberId) {
        log.info("Logout all devices for member: id={}", memberId);
        int revokedCount = refreshTokenRepository.revokeAllByMemberId(memberId);
        log.info("Revoked {} refresh tokens for member: id={}", revokedCount, memberId);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        log.info("Email verification request");

        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TOKEN_INVALID));

        verificationToken.verify();

        log.info("Email verified for member: id={}", verificationToken.getMember().getId());
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Resend verification email for: {}", email);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.isEmailVerified()) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Email is already verified");
        }

        // Create new verification token
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .member(member)
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        // Send verification email asynchronously
        emailService.sendVerificationEmail(
                member.getEmail(),
                member.getName(),
                verificationToken.getToken()
        );
        log.info("Verification email resent for member: id={}", member.getId());
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // Create password reset token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .member(member)
                .build();
        passwordResetTokenRepository.save(resetToken);

        // Send password reset email asynchronously
        emailService.sendPasswordResetEmail(
                member.getEmail(),
                member.getName(),
                resetToken.getToken()
        );
        log.info("Password reset email sent for member: id={}", member.getId());
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Password reset attempt with token");

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> new InvalidStateException(ErrorCode.TOKEN_INVALID));

        // Mark token as used
        resetToken.use();

        // Update member password
        Member member = resetToken.getMember();
        member.changePassword(passwordEncoder.encode(newPassword));

        log.info("Password reset successful for member: id={}", member.getId());
    }

    // ========== Private Helper Methods ==========

    private RefreshToken createRefreshToken(Member member, HttpServletRequest httpRequest) {
        String deviceInfo = extractDeviceInfo(httpRequest);
        String ipAddress = extractIpAddress(httpRequest);

        RefreshToken refreshToken = RefreshToken.builder()
                .member(member)
                .expirationDays(jwtTokenProvider.getRefreshTokenExpirationDays())
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 255)) : "Unknown";
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void validatePasswordMatch(MemberCreateRequest request) {
        if (!request.isPasswordMatched()) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Passwords do not match");
        }
    }

    private void validateEmailNotExists(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(ErrorCode.MEMBER_EMAIL_DUPLICATED);
        }
    }

    private void validatePassword(Member member, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private void validateMemberStatus(Member member) {
        if (!member.isEmailVerified()) {
            throw new AuthenticationException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new AuthenticationException(ErrorCode.MEMBER_SUSPENDED);
        }
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new AuthenticationException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
    }
}
