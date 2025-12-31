package platform.ecommerce.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import platform.ecommerce.domain.auth.EmailVerificationToken;
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
import platform.ecommerce.fixture.MemberFixture;
import platform.ecommerce.mapper.MemberMapper;
import platform.ecommerce.repository.EmailVerificationTokenRepository;
import platform.ecommerce.repository.MemberRepository;
import platform.ecommerce.repository.PasswordResetTokenRepository;
import platform.ecommerce.repository.RefreshTokenRepository;
import platform.ecommerce.security.JwtTokenProvider;
import platform.ecommerce.service.email.EmailService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * AuthService unit tests.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    @DisplayName("Register")
    class Register {

        @Test
        @DisplayName("Should register member with valid request")
        void register_withValidRequest_shouldSucceed() {
            // given
            MemberCreateRequest request = MemberCreateRequest.builder()
                    .email("test@example.com")
                    .password("Password1!")
                    .passwordConfirm("Password1!")
                    .name("TestUser")
                    .phone("010-1234-5678")
                    .build();

            Member member = MemberFixture.createPendingMember();
            MemberResponse expectedResponse = MemberResponse.builder()
                    .id(1L)
                    .email("test@example.com")
                    .name("TestUser")
                    .status(MemberStatus.PENDING)
                    .build();

            given(memberRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(memberRepository.save(any(Member.class))).willReturn(member);
            given(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                    .willReturn(mock(EmailVerificationToken.class));
            given(memberMapper.toResponse(any(Member.class))).willReturn(expectedResponse);

            // when
            MemberResponse result = authService.register(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo("test@example.com");
            verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
        }

        @Test
        @DisplayName("Should throw exception for mismatched passwords")
        void register_withMismatchedPassword_shouldThrowException() {
            // given
            MemberCreateRequest request = MemberCreateRequest.builder()
                    .email("test@example.com")
                    .password("Password1!")
                    .passwordConfirm("DifferentPassword1!")
                    .name("TestUser")
                    .build();

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("Should throw exception for duplicate email")
        void register_withDuplicateEmail_shouldThrowException() {
            // given
            MemberCreateRequest request = MemberCreateRequest.builder()
                    .email("existing@example.com")
                    .password("Password1!")
                    .passwordConfirm("Password1!")
                    .name("TestUser")
                    .build();

            given(memberRepository.existsByEmail("existing@example.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Login")
    class Login {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_withValidCredentials_shouldSucceed() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("Password1!")
                    .build();

            Member member = MemberFixture.createActiveMember();
            org.springframework.test.util.ReflectionTestUtils.setField(member, "id", 1L);

            RefreshToken refreshToken = mock(RefreshToken.class);
            given(refreshToken.getToken()).willReturn("refresh-token-value");

            MemberResponse memberResponse = MemberResponse.builder()
                    .id(1L)
                    .email("test@example.com")
                    .build();

            given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));
            given(passwordEncoder.matches("Password1!", member.getPassword())).willReturn(true);
            given(jwtTokenProvider.generateAccessToken(member)).willReturn("access-token");
            given(jwtTokenProvider.getAccessTokenExpirationSeconds()).willReturn(900L);
            given(jwtTokenProvider.getRefreshTokenExpirationDays()).willReturn(7L);
            given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(refreshToken);
            given(memberMapper.toResponse(member)).willReturn(memberResponse);

            given(httpServletRequest.getHeader("User-Agent")).willReturn("TestBrowser");
            given(httpServletRequest.getHeader("X-Forwarded-For")).willReturn(null);
            given(httpServletRequest.getRemoteAddr()).willReturn("127.0.0.1");

            // when
            LoginResponse result = authService.login(request, httpServletRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.tokens().accessToken()).isEqualTo("access-token");
            assertThat(result.member().id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception for non-existent email")
        void login_withNonExistentEmail_shouldThrowException() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("nonexistent@example.com")
                    .password("Password1!")
                    .build();

            given(memberRepository.findByEmail("nonexistent@example.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request, httpServletRequest))
                    .isInstanceOf(AuthenticationException.class);
        }

        @Test
        @DisplayName("Should throw exception for wrong password")
        void login_withWrongPassword_shouldThrowException() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("WrongPassword1!")
                    .build();

            Member member = MemberFixture.createActiveMember();
            given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));
            given(passwordEncoder.matches("WrongPassword1!", member.getPassword())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request, httpServletRequest))
                    .isInstanceOf(AuthenticationException.class);
        }

        @Test
        @DisplayName("Should throw exception for unverified email")
        void login_withUnverifiedEmail_shouldThrowException() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("Password1!")
                    .build();

            Member member = MemberFixture.createPendingMember(); // email not verified
            given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));
            given(passwordEncoder.matches("Password1!", member.getPassword())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.login(request, httpServletRequest))
                    .isInstanceOf(AuthenticationException.class);
        }

        @Test
        @DisplayName("Should throw exception for suspended member")
        void login_withSuspendedMember_shouldThrowException() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("Password1!")
                    .build();

            Member member = MemberFixture.createSuspendedMember();
            given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));
            given(passwordEncoder.matches("Password1!", member.getPassword())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.login(request, httpServletRequest))
                    .isInstanceOf(AuthenticationException.class);
        }
    }

    @Nested
    @DisplayName("Token Refresh")
    class TokenRefresh {

        @Test
        @DisplayName("Should refresh tokens with valid refresh token")
        void refreshToken_withValidToken_shouldSucceed() {
            // given
            TokenRefreshRequest request = TokenRefreshRequest.builder()
                    .refreshToken("valid-refresh-token")
                    .build();

            Member member = MemberFixture.createActiveMember();
            org.springframework.test.util.ReflectionTestUtils.setField(member, "id", 1L);

            RefreshToken refreshToken = mock(RefreshToken.class);
            given(refreshToken.getMember()).willReturn(member);
            given(refreshToken.rotate(anyLong())).willReturn("new-refresh-token");

            given(refreshTokenRepository.findValidToken(eq("valid-refresh-token"), any(LocalDateTime.class)))
                    .willReturn(Optional.of(refreshToken));
            given(jwtTokenProvider.generateAccessToken(member)).willReturn("new-access-token");
            given(jwtTokenProvider.getAccessTokenExpirationSeconds()).willReturn(900L);
            given(jwtTokenProvider.getRefreshTokenExpirationDays()).willReturn(7L);

            // when
            TokenResponse result = authService.refreshToken(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
            verify(refreshToken).rotate(7L);
        }

        @Test
        @DisplayName("Should throw exception for invalid refresh token")
        void refreshToken_withInvalidToken_shouldThrowException() {
            // given
            TokenRefreshRequest request = TokenRefreshRequest.builder()
                    .refreshToken("invalid-refresh-token")
                    .build();

            given(refreshTokenRepository.findValidToken(eq("invalid-refresh-token"), any(LocalDateTime.class)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(AuthenticationException.class);
        }
    }

    @Nested
    @DisplayName("Logout")
    class Logout {

        @Test
        @DisplayName("Should revoke refresh token on logout")
        void logout_withValidToken_shouldRevokeToken() {
            // given
            String refreshToken = "valid-refresh-token";
            RefreshToken token = mock(RefreshToken.class);
            Member member = MemberFixture.createActiveMember();
            org.springframework.test.util.ReflectionTestUtils.setField(member, "id", 1L);

            given(token.getMember()).willReturn(member);
            given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.of(token));

            // when
            authService.logout(refreshToken);

            // then
            verify(token).revoke();
        }

        @Test
        @DisplayName("Should handle logout with non-existent token gracefully")
        void logout_withNonExistentToken_shouldNotThrow() {
            // given
            String refreshToken = "non-existent-token";
            given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.empty());

            // when & then
            assertThatCode(() -> authService.logout(refreshToken)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should revoke all tokens on logout all devices")
        void logoutAll_shouldRevokeAllTokens() {
            // given
            Long memberId = 1L;
            given(refreshTokenRepository.revokeAllByMemberId(memberId)).willReturn(3);

            // when
            authService.logoutAll(memberId);

            // then
            verify(refreshTokenRepository).revokeAllByMemberId(memberId);
        }
    }

    @Nested
    @DisplayName("Email Verification")
    class EmailVerification {

        @Test
        @DisplayName("Should verify email with valid token")
        void verifyEmail_withValidToken_shouldSucceed() {
            // given
            String token = "valid-verification-token";
            EmailVerificationToken verificationToken = mock(EmailVerificationToken.class);
            Member member = MemberFixture.createPendingMember();

            given(verificationToken.getMember()).willReturn(member);
            given(emailVerificationTokenRepository.findByToken(token)).willReturn(Optional.of(verificationToken));

            // when
            authService.verifyEmail(token);

            // then
            verify(verificationToken).verify();
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void verifyEmail_withInvalidToken_shouldThrowException() {
            // given
            String token = "invalid-token";
            given(emailVerificationTokenRepository.findByToken(token)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.verifyEmail(token))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Resend Verification Email")
    class ResendVerification {

        @Test
        @DisplayName("Should create new verification token")
        void resendVerificationEmail_shouldCreateNewToken() {
            // given
            String email = "test@example.com";
            Member member = MemberFixture.createPendingMember();
            org.springframework.test.util.ReflectionTestUtils.setField(member, "id", 1L);

            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
            given(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                    .willReturn(mock(EmailVerificationToken.class));

            // when
            authService.resendVerificationEmail(email);

            // then
            verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
        }

        @Test
        @DisplayName("Should throw exception for non-existent email")
        void resendVerificationEmail_withNonExistentEmail_shouldThrowException() {
            // given
            String email = "nonexistent@example.com";
            given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.resendVerificationEmail(email))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception for already verified email")
        void resendVerificationEmail_withVerifiedEmail_shouldThrowException() {
            // given
            String email = "test@example.com";
            Member member = MemberFixture.createActiveMember(); // Already verified

            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> authService.resendVerificationEmail(email))
                    .isInstanceOf(InvalidStateException.class);
        }
    }
}
