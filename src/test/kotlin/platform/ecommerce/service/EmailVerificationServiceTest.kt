package platform.ecommerce.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import platform.ecommerce.config.EcommerceProperties
import platform.ecommerce.domain.EmailVerificationToken
import platform.ecommerce.domain.Member
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus
import platform.ecommerce.exception.TokenAlreadyUsedException
import platform.ecommerce.exception.TokenExpiredException
import platform.ecommerce.exception.TokenNotFoundException
import platform.ecommerce.repository.EmailVerificationTokenRepository
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class EmailVerificationServiceTest {

    @Mock
    private lateinit var emailVerificationTokenRepository: EmailVerificationTokenRepository

    @Mock
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var ecommerceProperties: EcommerceProperties

    @InjectMocks
    private lateinit var emailVerificationService: EmailVerificationService

    private lateinit var testMember: Member
    private lateinit var validToken: EmailVerificationToken

    @BeforeEach
    fun setUp() {
        testMember = Member(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe",
            phone = "010-1234-5678",
            role = MemberRole.CUSTOMER,
            status = MemberStatus.PENDING
        )

        validToken = EmailVerificationToken(
            token = UUID.randomUUID(),
            member = testMember,
            expiresAt = Instant.now().plusSeconds(3600)
        )
    }

    @Test
    fun `should verify email successfully with valid token`() {
        // Given
        whenever(emailVerificationTokenRepository.findByToken(validToken.token)).thenReturn(validToken)

        // When
        emailVerificationService.verifyEmail(validToken.token)

        // Then
        verify(memberService).activate(testMember)
        assertThat(validToken.verified).isTrue()
    }

    @Test
    fun `should throw TokenNotFoundException with non-existent token`() {
        // Given
        val nonExistentToken = UUID.randomUUID()
        whenever(emailVerificationTokenRepository.findByToken(nonExistentToken)).thenReturn(null)

        // When & Then
        assertThatThrownBy { emailVerificationService.verifyEmail(nonExistentToken) }
            .isInstanceOf(TokenNotFoundException::class.java)
            .hasMessageContaining("Token not found")

        verify(memberService, never()).activate(any())
    }

    @Test
    fun `should throw TokenExpiredException with expired token`() {
        // Given
        val expiredToken = EmailVerificationToken(
            token = UUID.randomUUID(),
            member = testMember,
            expiresAt = Instant.now().minusSeconds(3600) // 1 hour ago
        )
        whenever(emailVerificationTokenRepository.findByToken(expiredToken.token)).thenReturn(expiredToken)

        // When & Then
        assertThatThrownBy { emailVerificationService.verifyEmail(expiredToken.token) }
            .isInstanceOf(TokenExpiredException::class.java)
            .hasMessageContaining("Token expired")

        verify(memberService, never()).activate(any())
    }

    @Test
    fun `should throw TokenAlreadyUsedException with already used token`() {
        // Given
        validToken.verified = true
        whenever(emailVerificationTokenRepository.findByToken(validToken.token)).thenReturn(validToken)

        // When & Then
        assertThatThrownBy { emailVerificationService.verifyEmail(validToken.token) }
            .isInstanceOf(TokenAlreadyUsedException::class.java)
            .hasMessageContaining("Token already used")

        verify(memberService, never()).activate(any())
    }

    @Test
    fun `should change token verified field to true on successful verification`() {
        // Given
        whenever(emailVerificationTokenRepository.findByToken(validToken.token)).thenReturn(validToken)
        assertThat(validToken.verified).isFalse()

        // When
        emailVerificationService.verifyEmail(validToken.token)

        // Then
        assertThat(validToken.verified).isTrue()
    }

    @Test
    fun `should call memberService activate on successful verification`() {
        // Given
        whenever(emailVerificationTokenRepository.findByToken(validToken.token)).thenReturn(validToken)

        // When
        emailVerificationService.verifyEmail(validToken.token)

        // Then
        verify(memberService).activate(testMember)
    }
}
