package platform.ecommerce.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import platform.ecommerce.config.SecurityConfig
import platform.ecommerce.exception.MemberAlreadyActivated
import platform.ecommerce.exception.TokenAlreadyUsedException
import platform.ecommerce.exception.TokenExpiredException
import platform.ecommerce.exception.TokenNotFoundException
import platform.ecommerce.security.JwtUtil
import platform.ecommerce.service.EmailVerificationService
import java.util.*

@WebMvcTest(controllers = [EmailVerificationController::class])
@Import(SecurityConfig::class)
class EmailVerificationControllerTest {

    @Autowired
    private lateinit var mockMvcTester: MockMvcTester

    @MockitoBean
    private lateinit var emailVerificationService: EmailVerificationService

    @MockitoBean
    private lateinit var jwtUtil: JwtUtil

    @MockitoBean
    private lateinit var userDetailsService: UserDetailsService

    @Test
    fun `should return 200 OK with valid token`() {
        // Given
        val validToken = UUID.randomUUID()

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/verify-email/$validToken"))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(true) }
            .extractingPath("$.message").isEqualTo("Successfully verified")

        verify(emailVerificationService).verifyEmail(validToken)
    }

    @Test
    fun `should return 404 Not Found with non-existent token`() {
        // Given
        val nonExistentToken = UUID.randomUUID()
        given(emailVerificationService.verifyEmail(any()))
            .willThrow(TokenNotFoundException("Token not found"))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/verify-email/$nonExistentToken"))
            .hasStatus(HttpStatus.NOT_FOUND)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(false) }
            .extractingPath("$.message").isEqualTo("Token not found")
    }

    @Test
    fun `should return 410 Gone with expired token`() {
        // Given
        val expiredToken = UUID.randomUUID()
        given(emailVerificationService.verifyEmail(any()))
            .willThrow(TokenExpiredException("Token expired"))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/verify-email/$expiredToken"))
            .hasStatus(HttpStatus.GONE)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(false) }
            .extractingPath("$.message").isEqualTo("Token expired")
    }

    @Test
    fun `should return 409 Conflict with already used token`() {
        // Given
        val usedToken = UUID.randomUUID()
        given(emailVerificationService.verifyEmail(any()))
            .willThrow(TokenAlreadyUsedException("Token already used"))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/verify-email/$usedToken"))
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(false) }
            .extractingPath("$.message").isEqualTo("Token already used")
    }

    @Test
    fun `should return 409 Conflict for already ACTIVE member's token`() {
        // Given
        val activeToken = UUID.randomUUID()
        given(emailVerificationService.verifyEmail(any()))
            .willThrow(MemberAlreadyActivated("Member already activated"))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/verify-email/$activeToken"))
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(false) }
            .extractingPath("$.message").isEqualTo("Member already activated")
    }
}
