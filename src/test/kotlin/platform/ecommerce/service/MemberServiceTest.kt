package platform.ecommerce.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import platform.ecommerce.domain.Member
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus
import platform.ecommerce.exception.DuplicateEmailException
import platform.ecommerce.repository.MemberRepository

@ExtendWith(MockitoExtension::class)
class MemberServiceTest {

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    private lateinit var memberService: MemberService

    @Test
    fun `register should create member with encoded password when email does not exist`() {
        // Given
        val request = MemberRegister(
            email = "test@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe",
            phone = "1234567890"
        )
        val encodedPassword = "encodedPassword123"

        whenever(memberRepository.existsByEmail(request.email)).thenReturn(false)
        whenever(passwordEncoder.encode(request.password)).thenReturn(encodedPassword)
        whenever(memberRepository.save(any<Member>())).thenAnswer { it.arguments[0] as Member }

        // When
        val result = memberService.register(request)

        // Then
        assertThat(result).isNotNull
        assertThat(result.email).isEqualTo(request.email)
        assertThat(result.passwordHash).isEqualTo(encodedPassword)
        assertThat(result.firstName).isEqualTo(request.firstName)
        assertThat(result.lastName).isEqualTo(request.lastName)
        assertThat(result.phone).isEqualTo(request.phone)
        assertThat(result.role).isEqualTo(MemberRole.CUSTOMER)
        assertThat(result.status).isEqualTo(MemberStatus.PENDING)

        verify(memberRepository).existsByEmail(request.email)
        verify(passwordEncoder).encode(request.password)
        verify(memberRepository).save(any<Member>())
    }

    @Test
    fun `register should throw DuplicateEmailException when email already exists`() {
        // Given
        val request = MemberRegister(
            email = "existing@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe"
        )

        whenever(memberRepository.existsByEmail(request.email)).thenReturn(true)

        // When & Then
        assertThatThrownBy { memberService.register(request) }
            .isInstanceOf(DuplicateEmailException::class.java)
            .hasMessage("Email already exists: existing@example.com")

        verify(memberRepository).existsByEmail(request.email)
        verify(passwordEncoder, never()).encode(any())
        verify(memberRepository, never()).save(any())
    }

}
