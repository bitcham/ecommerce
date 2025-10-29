package platform.ecommerce.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import platform.ecommerce.domain.Member
import platform.ecommerce.dto.request.LoginRequest
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.dto.response.MemberResponse
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus
import platform.ecommerce.exception.InvalidCredentialsException
import platform.ecommerce.fixture.MemberFixture
import platform.ecommerce.mapper.MemberMapper
import platform.ecommerce.security.JwtUtil
import platform.ecommerce.service.impl.AuthServiceImpl
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var memberMapper: MemberMapper

    @InjectMocks
    private lateinit var authService: AuthServiceImpl

    @Mock
    private lateinit var authenticationManager: AuthenticationManager

    @Mock
    private lateinit var jwtUtil: JwtUtil

    @Test
    fun `register should call memberService and map to response`() {
        // Given
        val request = MemberRegister(
            email = "test@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe",
            phone = "1234567890"
        )

        val member = MemberFixture.createMember(
            email = request.email,
            passwordHash = "encodedPassword",
            firstName = request.firstName,
            lastName = request.lastName,
            phone = request.phone
        )

        val expectedResponse = MemberResponse(
            id = UUID.randomUUID(),
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            phone = request.phone,
            role = MemberRole.CUSTOMER,
            status = MemberStatus.PENDING,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        whenever(memberService.register(request)).thenReturn(member)
        whenever(memberMapper.toResponse(member)).thenReturn(expectedResponse)

        // When
        val result = authService.register(request)

        // Then
        assertThat(result).isNotNull
        assertThat(result.email).isEqualTo(request.email)
        assertThat(result.firstName).isEqualTo(request.firstName)
        assertThat(result.lastName).isEqualTo(request.lastName)
        assertThat(result.phone).isEqualTo(request.phone)
        assertThat(result.role).isEqualTo(MemberRole.CUSTOMER)
        assertThat(result.status).isEqualTo(MemberStatus.PENDING)

        verify(memberService).register(request)
        verify(memberMapper).toResponse(member)
    }

    @Test
    fun `Should return login response with tokens when authentication succeeds`() {
        // Given
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )

        val memberId = UUID.randomUUID()
        val member = MemberFixture.createMember(
            id = memberId,
            email = request.email,
            passwordHash = "encodedPassword",
            firstName = "John",
            lastName = "Doe"
        )

        val userDetails = User.builder()
            .username(request.email)
            .password("encodedPassword")
            .authorities(setOf(SimpleGrantedAuthority("ROLE_CUSTOMER")))
            .build()

        val authentication = UsernamePasswordAuthenticationToken(
            userDetails,
            "password123",
            userDetails.authorities
        )

        whenever(authenticationManager.authenticate(any())).thenReturn(authentication)
        whenever(memberService.findByEmail(request.email)).thenReturn(member)
        whenever(jwtUtil.generateAccessToken(userDetails)).thenReturn("access-token")
        whenever(jwtUtil.generateRefreshToken(userDetails)).thenReturn("refresh-token")
        whenever(jwtUtil.getAccessTokenExpiration()).thenReturn(3600000L)

        // When
        val result = authService.login(request)

        // Then
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(memberId)
        assertThat(result.email).isEqualTo(request.email)
        assertThat(result.role).isEqualTo("CUSTOMER")
        assertThat(result.accessToken).isEqualTo("access-token")
        assertThat(result.refreshToken).isEqualTo("refresh-token")
        assertThat(result.tokenType).isEqualTo("Bearer")
        assertThat(result.expiresIn).isEqualTo(3600000L)

        verify(authenticationManager).authenticate(any())
        verify(memberService).findByEmail(request.email)
        verify(jwtUtil).generateAccessToken(userDetails)
        verify(jwtUtil).generateRefreshToken(userDetails)
    }

    @Test
    fun `Should throw InvalidCredentialsException when authentication fails`() {
        // Given
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        whenever(authenticationManager.authenticate(any()))
            .thenThrow(BadCredentialsException("Bad credentials"))

        // When & Then
        assertThatThrownBy { authService.login(request) }
            .isInstanceOf(InvalidCredentialsException::class.java)
            .hasMessageContaining("Invalid username or password")

        verify(authenticationManager).authenticate(any())
    }

}
