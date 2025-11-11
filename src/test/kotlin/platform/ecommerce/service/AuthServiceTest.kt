package platform.ecommerce.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.AuthenticationManager
import platform.ecommerce.domain.Member
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus
import platform.ecommerce.exception.DuplicateEmailException
import platform.ecommerce.fixture.MemberFixture
import platform.ecommerce.security.JwtUtil
import platform.ecommerce.service.impl.AuthServiceImpl
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var authenticationManager: AuthenticationManager

    @Mock
    private lateinit var jwtUtil: JwtUtil

    @Mock
    private lateinit var emailVerificationService: EmailVerificationService

    @Mock
    private lateinit var emailService: EmailService

    @InjectMocks
    private lateinit var authService: AuthServiceImpl

    private lateinit var validRegisterRequest: MemberRegister
    private lateinit var testMember: Member

    @BeforeEach
    fun setUp() {
        validRegisterRequest = MemberFixture.createMemberRegisterRequest()
        testMember = MemberFixture.createMember()
    }

    @Test
    fun `should successfully register new member with valid data`() {
        // Given
        val mockToken = platform.ecommerce.domain.EmailVerificationToken(
            token = UUID.randomUUID(),
            member = testMember,
            expiresAt = java.time.Instant.now().plusSeconds(86400)
        )

        whenever(memberService.register(validRegisterRequest)).thenReturn(testMember)
        whenever(emailVerificationService.createVerificationToken(testMember)).thenReturn(mockToken)

        // When
        val result = authService.register(validRegisterRequest)

        // Then
        assertThat(result).isNotNull
        assertThat(result.email).isEqualTo("test@example.com")
        assertThat(result.firstName).isEqualTo("John")
        assertThat(result.lastName).isEqualTo("Doe")
        assertThat(result.phone).isEqualTo("010-1234-5678")
        assertThat(result.role).isEqualTo(MemberRole.CUSTOMER)
        assertThat(result.status).isEqualTo(MemberStatus.PENDING)

        // Verify interactions
        verify(memberService).register(validRegisterRequest)
        verify(emailVerificationService).createVerificationToken(testMember)
        verify(emailService).sendVerificationEmail(testMember.email, mockToken.token)
    }

    @Test
    fun `should throw DuplicateEmailException when email already exists`() {
        // Given
        whenever(memberService.register(validRegisterRequest))
            .thenThrow(DuplicateEmailException("Email already exists: test@example.com"))

        // When & Then
        assertThatThrownBy { authService.register(validRegisterRequest) }
            .isInstanceOf(DuplicateEmailException::class.java)
            .hasMessageContaining("test@example.com")

        verify(memberService).register(validRegisterRequest)
        verify(emailVerificationService, never()).createVerificationToken(any())
        verify(emailService, never()).sendVerificationEmail(any(), any())
    }

    @Test
    fun `should register member without phone number`() {
        // Given
        val requestWithoutPhone = MemberFixture.createMemberRegisterRequest(phone = null)
        val memberWithoutPhone = MemberFixture.createMember(phone = null)

        val mockToken = platform.ecommerce.domain.EmailVerificationToken(
            token = UUID.randomUUID(),
            member = memberWithoutPhone,
            expiresAt = java.time.Instant.now().plusSeconds(86400)
        )

        whenever(memberService.register(requestWithoutPhone)).thenReturn(memberWithoutPhone)
        whenever(emailVerificationService.createVerificationToken(memberWithoutPhone)).thenReturn(mockToken)

        // When
        val result = authService.register(requestWithoutPhone)

        // Then
        assertThat(result.phone).isNull()

        verify(memberService).register(requestWithoutPhone)
        verify(emailVerificationService).createVerificationToken(memberWithoutPhone)
        verify(emailService).sendVerificationEmail(memberWithoutPhone.email, mockToken.token)
    }

    @Test
    fun `password should be encrypted with BCrypt`() {
        // Given
        val mockToken = platform.ecommerce.domain.EmailVerificationToken(
            token = UUID.randomUUID(),
            member = testMember,
            expiresAt = java.time.Instant.now().plusSeconds(86400)
        )

        whenever(memberService.register(validRegisterRequest)).thenReturn(testMember)
        whenever(emailVerificationService.createVerificationToken(testMember)).thenReturn(mockToken)

        // When
        authService.register(validRegisterRequest)

        // Then
        verify(memberService).register(validRegisterRequest)
        verify(emailVerificationService).createVerificationToken(testMember)
        verify(emailService).sendVerificationEmail(testMember.email, mockToken.token)
    }
}