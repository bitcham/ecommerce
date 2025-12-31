package platform.ecommerce.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import platform.ecommerce.config.TestRateLimiterConfig;
import platform.ecommerce.config.TestRateLimiterConfig.TestableRateLimiterRegistry;
import platform.ecommerce.service.AuthService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Rate Limiter tests for AuthController endpoints.
 * Uses MockMvcTester (Spring Boot 3.4+) with AssertJ fluent assertions.
 *
 * Test strategy: Exhaust rate limit by making requests (ignoring response status),
 * then verify the next request returns 429 Too Many Requests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRateLimiterConfig.class)
@DisplayName("AuthController Rate Limiter Tests")
class AuthControllerRateLimiterTest {

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private TestableRateLimiterRegistry rateLimiterRegistry;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void resetRateLimiters() {
        // Clear all rate limiters for complete test isolation
        rateLimiterRegistry.clearAll();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/forgot-password")
    class ForgotPasswordRateLimit {

        private static final String ENDPOINT = "/api/v1/auth/forgot-password";
        private static final String REQUEST_BODY = """
                {"email": "test@example.com"}
                """;

        @Test
        @DisplayName("Should return 429 when rate limit exceeded")
        void shouldReturn429WhenRateLimitExceeded() {
            // Given: Exhaust rate limit (2 requests allowed in test config)
            for (int i = 0; i < 2; i++) {
                mockMvc.post()
                        .uri(ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY)
                        .exchange();
            }

            // When & Then: 3rd request should be rate limited
            assertThat(mockMvc.post()
                    .uri(ENDPOINT)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REQUEST_BODY))
                    .hasStatus(429)
                    .bodyJson()
                    .extractingPath("$.error.code")
                    .isEqualTo("RATE_LIMIT_EXCEEDED");
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/resend-verification")
    class ResendVerificationRateLimit {

        private static final String ENDPOINT = "/api/v1/auth/resend-verification";
        private static final String REQUEST_BODY = """
                {"email": "test@example.com"}
                """;

        @Test
        @DisplayName("Should return 429 when rate limit exceeded")
        void shouldReturn429WhenRateLimitExceeded() {
            // Given: Exhaust rate limit
            for (int i = 0; i < 2; i++) {
                mockMvc.post()
                        .uri(ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY)
                        .exchange();
            }

            // When & Then: 3rd request should be rate limited
            assertThat(mockMvc.post()
                    .uri(ENDPOINT)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REQUEST_BODY))
                    .hasStatus(429)
                    .bodyJson()
                    .extractingPath("$.error.code")
                    .isEqualTo("RATE_LIMIT_EXCEEDED");
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/reset-password")
    class ResetPasswordRateLimit {

        private static final String ENDPOINT = "/api/v1/auth/reset-password";
        private static final String REQUEST_BODY = """
                {"token": "test-token", "newPassword": "NewPassword1!"}
                """;

        @Test
        @DisplayName("Should return 429 when rate limit exceeded")
        void shouldReturn429WhenRateLimitExceeded() {
            // Given: Exhaust rate limit
            for (int i = 0; i < 2; i++) {
                mockMvc.post()
                        .uri(ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY)
                        .exchange();
            }

            // When & Then: 3rd request should be rate limited
            assertThat(mockMvc.post()
                    .uri(ENDPOINT)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REQUEST_BODY))
                    .hasStatus(429)
                    .bodyJson()
                    .extractingPath("$.error.code")
                    .isEqualTo("RATE_LIMIT_EXCEEDED");
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class RefreshTokenRateLimit {

        private static final String ENDPOINT = "/api/v1/auth/refresh";
        private static final String REQUEST_BODY = """
                {"refreshToken": "test-refresh-token"}
                """;

        @Test
        @DisplayName("Should return 429 when rate limit exceeded")
        void shouldReturn429WhenRateLimitExceeded() {
            // Given: Exhaust rate limit
            for (int i = 0; i < 2; i++) {
                mockMvc.post()
                        .uri(ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY)
                        .exchange();
            }

            // When & Then: 3rd request should be rate limited
            assertThat(mockMvc.post()
                    .uri(ENDPOINT)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REQUEST_BODY))
                    .hasStatus(429)
                    .bodyJson()
                    .extractingPath("$.error.code")
                    .isEqualTo("RATE_LIMIT_EXCEEDED");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/verify-email")
    class VerifyEmailRateLimit {

        private static final String ENDPOINT = "/api/v1/auth/verify-email";

        @Test
        @DisplayName("Should return 429 when rate limit exceeded")
        void shouldReturn429WhenRateLimitExceeded() {
            // Given: Exhaust rate limit
            for (int i = 0; i < 2; i++) {
                mockMvc.get()
                        .uri(ENDPOINT)
                        .param("token", "test-token-" + i)
                        .exchange();
            }

            // When & Then: 3rd request should be rate limited
            assertThat(mockMvc.get()
                    .uri(ENDPOINT)
                    .param("token", "test-token-3"))
                    .hasStatus(429)
                    .bodyJson()
                    .extractingPath("$.error.code")
                    .isEqualTo("RATE_LIMIT_EXCEEDED");
        }
    }

    @Nested
    @DisplayName("Logout Endpoints - No Rate Limit")
    class LogoutNoRateLimit {

        @Test
        @DisplayName("POST /logout should not have rate limit")
        void logoutShouldNotHaveRateLimit() {
            String requestBody = """
                    {"refreshToken": "test-refresh-token"}
                    """;

            // Should be able to make many requests without 429
            for (int i = 0; i < 10; i++) {
                var result = mockMvc.post()
                        .uri("/api/v1/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .exchange();
                assertThat(result.getResponse().getStatus()).isNotEqualTo(429);
            }
        }

        @Test
        @DisplayName("POST /logout-all should not have rate limit")
        void logoutAllShouldNotHaveRateLimit() {
            // Should be able to make many requests without 429
            for (int i = 0; i < 10; i++) {
                var result = mockMvc.post()
                        .uri("/api/v1/auth/logout-all")
                        .with(csrf())
                        .param("memberId", "1")
                        .exchange();
                assertThat(result.getResponse().getStatus()).isNotEqualTo(429);
            }
        }
    }

    @Nested
    @DisplayName("Existing Rate Limiters Verification")
    class ExistingRateLimiters {

        @Test
        @DisplayName("POST /login should return 429 when rate limit exceeded")
        void loginShouldReturn429WhenRateLimitExceeded() {
            String requestBody = """
                    {"email": "test@example.com", "password": "Password1!"}
                    """;

            // Given: Exhaust rate limit
            for (int i = 0; i < 2; i++) {
                mockMvc.post()
                        .uri("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .exchange();
            }

            // When & Then: 3rd request should be rate limited
            assertThat(mockMvc.post()
                    .uri("/api/v1/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .hasStatus(429)
                    .bodyJson()
                    .extractingPath("$.error.code")
                    .isEqualTo("RATE_LIMIT_EXCEEDED");
        }

        @Test
        @DisplayName("POST /register should return 429 when rate limit exceeded")
        void registerShouldReturn429WhenRateLimitExceeded() {
            String requestBodyTemplate = """
                    {
                        "email": "test%d@example.com",
                        "password": "Password1!",
                        "passwordConfirm": "Password1!",
                        "name": "TestUser",
                        "phone": "010-1234-5678"
                    }
                    """;

            // Given: Exhaust rate limit
            for (int i = 0; i < 2; i++) {
                mockMvc.post()
                        .uri("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyTemplate.formatted(i))
                        .exchange();
            }

            // When & Then: 3rd request should be rate limited
            assertThat(mockMvc.post()
                    .uri("/api/v1/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBodyTemplate.formatted(99)))
                    .hasStatus(429)
                    .bodyJson()
                    .extractingPath("$.error.code")
                    .isEqualTo("RATE_LIMIT_EXCEEDED");
        }
    }

    @Nested
    @DisplayName("Error Response Format")
    class ErrorResponseFormat {

        @Test
        @DisplayName("Rate limit error should have proper format")
        void rateLimitErrorShouldHaveProperFormat() {
            String requestBody = """
                    {"email": "format-test@example.com", "password": "Password1!"}
                    """;

            // Exhaust rate limit
            for (int i = 0; i < 2; i++) {
                mockMvc.post()
                        .uri("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .exchange();
            }

            // Verify error response format
            assertThat(mockMvc.post()
                    .uri("/api/v1/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .hasStatus(429)
                    .bodyJson()
                    .extractingPath("$.success").isEqualTo(false);
        }
    }
}
