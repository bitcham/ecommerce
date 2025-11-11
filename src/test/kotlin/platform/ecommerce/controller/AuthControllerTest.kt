package platform.ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import platform.ecommerce.config.SecurityConfig
import platform.ecommerce.domain.Member
import platform.ecommerce.dto.request.LoginRequest
import platform.ecommerce.dto.response.MemberResponse
import platform.ecommerce.exception.DuplicateEmailException
import platform.ecommerce.fixture.MemberFixture
import platform.ecommerce.mapper.MemberMapper
import platform.ecommerce.security.JwtUtil
import platform.ecommerce.service.AuthService
import java.util.*

@WebMvcTest(controllers = [AuthController::class])
@Import(SecurityConfig::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvcTester: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var authService: AuthService

    @MockitoBean
    private lateinit var jwtUtil: JwtUtil

    @MockitoBean
    private lateinit var memberMapper: MemberMapper

    @MockitoBean
    private lateinit var userDetailsService: UserDetailsService

    private lateinit var member: Member
    private lateinit var memberResponse: MemberResponse
    private lateinit var memberResponseWithoutPhone: MemberResponse

    @BeforeEach
    fun setUp() {
        member = MemberFixture.createMember(
            email = "customer@example.com",
            password = "hashedpassword"
        )

        memberResponse = MemberFixture.createMemberResponse(
            id = member.id!!,
            email = "customer@example.com"
        )

        memberResponseWithoutPhone = MemberFixture.createMemberResponse(
            id = member.id!!,
            email = "customer@example.com",
            phone = null
        )
    }

    @Test
    fun `should successfully register member with valid request and return 201 Created`() {
        // Given
        given(authService.register(any())).willReturn(member)
        given(memberMapper.toResponse(member)).willReturn(memberResponse)
        val validRequest = MemberFixture.createMemberRegisterRequest(
            email = "customer@example.com",
            password = "password123"
        )

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(true) }
            .hasPathSatisfying("$.data.email") { assertThat(it).isEqualTo("customer@example.com") }
            .hasPathSatisfying("$.data.firstName") { assertThat(it).isEqualTo("John") }
            .hasPathSatisfying("$.data.lastName") { assertThat(it).isEqualTo("Doe") }
            .hasPathSatisfying("$.data.phone") { assertThat(it).isEqualTo("010-1234-5678") }
            .hasPathSatisfying("$.data.role") { assertThat(it).isEqualTo("CUSTOMER") }
            .hasPathSatisfying("$.data.status") { assertThat(it).isEqualTo("PENDING") }
    }

    @Test
    fun `should successfully register member without phone number`() {
        // Given
        val memberWithoutPhone = MemberFixture.createMember(
            email = "customer@example.com",
            password = "hashedpassword",
            phone = null
        )

        given(authService.register(any())).willReturn(memberWithoutPhone)
        given(memberMapper.toResponse(memberWithoutPhone)).willReturn(memberResponseWithoutPhone)
        val requestWithoutPhone = MemberFixture.createMemberRegisterRequest(
            email = "customer@example.com",
            password = "password123",
            phone = null
        )

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestWithoutPhone)))
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(true) }
            .hasPathSatisfying("$.data.phone"){ assertThat(it).isNull() }
    }

    @Test
    fun `should return 409 Conflict when email already exists`() {
        // Given
        given(authService.register(any()))
            .willThrow(DuplicateEmailException("Email already exists: customer@example.com"))
        val validRequest = MemberFixture.createMemberRegisterRequest(
            email = "customer@example.com",
            password = "password123"
        )

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(false) }
            .extractingPath("$.message").isEqualTo("Email already exists: customer@example.com")
    }

    @Test
    fun `should return 400 Bad Request when email is invalid or missing`() {
        // Test missing email
        val requestWithoutEmail = objectMapper.writeValueAsString(mapOf(
            "password" to "password123",
            "firstName" to "John",
            "lastName" to "Doe"
        ))

        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestWithoutEmail))
            .hasStatus(HttpStatus.BAD_REQUEST)

        // Test invalid email format
        val invalidEmailRequest = objectMapper.writeValueAsString(mapOf(
            "email" to "invalid-email",
            "password" to "password123",
            "firstName" to "John",
            "lastName" to "Doe"
        ))

        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidEmailRequest))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return 400 Bad Request when password validation fails`() {
        // Test missing password
        val requestWithoutPassword = objectMapper.writeValueAsString(mapOf(
            "email" to "customer@example.com",
            "firstName" to "John",
            "lastName" to "Doe"
        ))

        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestWithoutPassword))
            .hasStatus(HttpStatus.BAD_REQUEST)

        // Test short password
        val shortPasswordRequest = objectMapper.writeValueAsString(mapOf(
            "email" to "customer@example.com",
            "password" to "short",
            "firstName" to "John",
            "lastName" to "Doe"
        ))

        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(shortPasswordRequest))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return 400 Bad Request when name validation fails`() {
        // Test missing firstName
        val requestWithoutFirstName = objectMapper.writeValueAsString(mapOf(
            "email" to "customer@example.com",
            "password" to "password123",
            "lastName" to "Doe"
        ))

        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestWithoutFirstName))
            .hasStatus(HttpStatus.BAD_REQUEST)

        // Test firstName exceeds 50 characters
        val longFirstNameRequest = objectMapper.writeValueAsString(mapOf(
            "email" to "customer@example.com",
            "password" to "password123",
            "firstName" to "A".repeat(51),
            "lastName" to "Doe"
        ))

        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(longFirstNameRequest))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return 400 Bad Request when phone exceeds 20 characters`() {
        // Given
        val longPhoneRequest = objectMapper.writeValueAsString(mapOf(
            "email" to "customer@example.com",
            "password" to "password123",
            "firstName" to "John",
            "lastName" to "Doe",
            "phone" to "0".repeat(21)
        ))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(longPhoneRequest))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }

    // ========== Login Tests ==========

    @Test
    fun `should return 200 OK and tokens when login with valid credentials`() {
        // Given
        val loginResponse = platform.ecommerce.dto.response.LoginResponse(
            id = UUID.randomUUID(),
            email = "customer@example.com",
            role = "CUSTOMER",
            accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token",
            refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.refresh",
            tokenType = "Bearer",
            expiresIn = 3600000L
        )
        given(authService.login(any())).willReturn(loginResponse)

        val validLoginRequest = objectMapper.writeValueAsString(LoginRequest(
            email = "customer@example.com",
            password = "password123"
        ))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validLoginRequest))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(true) }
            .hasPathSatisfying("$.data.id") { assertThat(it).isNotNull() }
            .hasPathSatisfying("$.data.email") { assertThat(it).isEqualTo("customer@example.com") }
            .hasPathSatisfying("$.data.role") { assertThat(it).isEqualTo("CUSTOMER") }
            .hasPathSatisfying("$.data.accessToken") { assertThat(it).isNotNull() }
            .hasPathSatisfying("$.data.refreshToken") { assertThat(it).isNotNull() }
            .hasPathSatisfying("$.data.tokenType") { assertThat(it).isEqualTo("Bearer") }
            .hasPathSatisfying("$.data.expiresIn") { assertThat(it).isEqualTo(3600000) }
    }

    @Test
    fun `should return 401 Unauthorized when email doesn't exist`() {
        // Given
        given(authService.login(any()))
            .willThrow(platform.ecommerce.exception.InvalidCredentialsException("Invalid email or password"))

        val invalidEmailRequest = objectMapper.writeValueAsString(LoginRequest(
            email = "nonexistent@example.com",
            password = "password123"
        ))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidEmailRequest))
            .hasStatus(HttpStatus.UNAUTHORIZED)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(false) }
            .extractingPath("$.message").isEqualTo("Invalid email or password")
    }

    @Test
    fun `should return 401 Unauthorized when password is incorrect`() {
        // Given
        given(authService.login(any()))
            .willThrow(platform.ecommerce.exception.InvalidCredentialsException("Invalid email or password"))

        val wrongPasswordRequest = objectMapper.writeValueAsString(LoginRequest(
            email = "customer@example.com",
            password = "wrongpassword"
        ))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(wrongPasswordRequest))
            .hasStatus(HttpStatus.UNAUTHORIZED)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(false) }
            .extractingPath("$.message").isEqualTo("Invalid email or password")
    }

    @Test
    fun `should return 401 Unauthorized when member status is PENDING`() {
        // Given
        given(authService.login(any()))
            .willThrow(platform.ecommerce.exception.InvalidCredentialsException("Invalid email or password"))

        val pendingMemberRequest = objectMapper.writeValueAsString(LoginRequest(
            email = "pending@example.com",
            password = "password123"
        ))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(pendingMemberRequest))
            .hasStatus(HttpStatus.UNAUTHORIZED)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(false) }
    }

    @Test
    fun `should return 401 Unauthorized when member status is INACTIVE`() {
        // Given
        given(authService.login(any()))
            .willThrow(platform.ecommerce.exception.InvalidCredentialsException("Invalid email or password"))

        val inactiveMemberRequest = objectMapper.writeValueAsString(LoginRequest(
            email = "inactive@example.com",
            password = "password123"
        ))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(inactiveMemberRequest))
            .hasStatus(HttpStatus.UNAUTHORIZED)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(false) }
    }

    @Test
    fun `should return 400 Bad Request when email is missing in login request`() {
        // Given
        val requestWithoutEmail = objectMapper.writeValueAsString(mapOf(
            "password" to "password123"
        ))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestWithoutEmail))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return 400 Bad Request when password is missing in login request`() {
        // Given
        val requestWithoutPassword = objectMapper.writeValueAsString(mapOf(
            "email" to "customer@example.com"
        ))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestWithoutPassword))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return 400 Bad Request when password is too short in login request`() {
        // Given
        val shortPasswordRequest = objectMapper.writeValueAsString(mapOf(
            "email" to "customer@example.com",
            "password" to "short"
        ))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(shortPasswordRequest))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }
}