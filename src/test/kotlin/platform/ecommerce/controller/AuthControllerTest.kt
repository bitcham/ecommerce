package platform.ecommerce.controller

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
import platform.ecommerce.dto.response.MemberResponse
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus
import platform.ecommerce.exception.DuplicateEmailException
import platform.ecommerce.security.JwtUtil
import platform.ecommerce.service.AuthService
import java.time.Instant
import java.util.*

@WebMvcTest(controllers = [AuthController::class])
@Import(SecurityConfig::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvcTester: MockMvcTester

    @MockitoBean
    private lateinit var authService: AuthService

    @MockitoBean
    private lateinit var jwtUtil: JwtUtil

    @MockitoBean
    private lateinit var userDetailsService: UserDetailsService

    private lateinit var memberResponse: MemberResponse

    @BeforeEach
    fun setUp() {
        memberResponse = MemberResponse(
            id = UUID.randomUUID(),
            email = "customer@example.com",
            firstName = "John",
            lastName = "Doe",
            phone = "010-1234-5678",
            role = MemberRole.CUSTOMER,
            status = MemberStatus.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    @Test
    fun `should successfully register member with valid request and return 201 Created`() {
        // Given
        given(authService.register(any())).willReturn(memberResponse)
        val validRequest = """
            {
                "email": "customer@example.com",
                "password": "password123",
                "firstName": "John",
                "lastName": "Doe",
                "phone": "010-1234-5678"
            }
        """.trimIndent()

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequest))
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
        val responseWithoutPhone = memberResponse.copy(phone = null)
        given(authService.register(any())).willReturn(responseWithoutPhone)
        val requestWithoutPhone = """
            {
                "email": "customer@example.com",
                "password": "password123",
                "firstName": "John",
                "lastName": "Doe"
            }
        """.trimIndent()

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestWithoutPhone))
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
        val validRequest = """
            {
                "email": "customer@example.com",
                "password": "password123",
                "firstName": "John",
                "lastName": "Doe"
            }
        """.trimIndent()

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequest))
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(false) }
            .extractingPath("$.message").isEqualTo("Email already exists: customer@example.com")
    }

    @Test
    fun `should return 400 Bad Request when email is invalid or missing`() {
        // Test missing email
        val requestWithoutEmail = """
            {
                "password": "password123",
                "firstName": "John",
                "lastName": "Doe"
            }
        """.trimIndent()

        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestWithoutEmail))
            .hasStatus(HttpStatus.BAD_REQUEST)

        // Test invalid email format
        val invalidEmailRequest = """
            {
                "email": "invalid-email",
                "password": "password123",
                "firstName": "John",
                "lastName": "Doe"
            }
        """.trimIndent()

        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidEmailRequest))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return 400 Bad Request when password validation fails`() {
        // Test missing password
        val requestWithoutPassword = """
            {
                "email": "customer@example.com",
                "firstName": "John",
                "lastName": "Doe"
            }
        """.trimIndent()

        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestWithoutPassword))
            .hasStatus(HttpStatus.BAD_REQUEST)

        // Test short password
        val shortPasswordRequest = """
            {
                "email": "customer@example.com",
                "password": "short",
                "firstName": "John",
                "lastName": "Doe"
            }
        """.trimIndent()

        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(shortPasswordRequest))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return 400 Bad Request when name validation fails`() {
        // Test missing firstName
        val requestWithoutFirstName = """
            {
                "email": "customer@example.com",
                "password": "password123",
                "lastName": "Doe"
            }
        """.trimIndent()

        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestWithoutFirstName))
            .hasStatus(HttpStatus.BAD_REQUEST)

        // Test firstName exceeds 50 characters
        val longFirstNameRequest = """
            {
                "email": "customer@example.com",
                "password": "password123",
                "firstName": "${"A".repeat(51)}",
                "lastName": "Doe"
            }
        """.trimIndent()

        assertThat(mockMvcTester.post()
            .uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(longFirstNameRequest))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return 400 Bad Request when phone exceeds 20 characters`() {
        // Given
        val longPhoneRequest = """
            {
                "email": "customer@example.com",
                "password": "password123",
                "firstName": "John",
                "lastName": "Doe",
                "phone": "${"0".repeat(21)}"
            }
        """.trimIndent()

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

        val validLoginRequest = """
            {
                "email": "customer@example.com",
                "password": "password123"
            }
        """.trimIndent()

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

        val invalidEmailRequest = """
            {
                "email": "nonexistent@example.com",
                "password": "password123"
            }
        """.trimIndent()

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

        val wrongPasswordRequest = """
            {
                "email": "customer@example.com",
                "password": "wrongpassword"
            }
        """.trimIndent()

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

        val pendingMemberRequest = """
            {
                "email": "pending@example.com",
                "password": "password123"
            }
        """.trimIndent()

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

        val inactiveMemberRequest = """
            {
                "email": "inactive@example.com",
                "password": "password123"
            }
        """.trimIndent()

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
        val requestWithoutEmail = """
            {
                "password": "password123"
            }
        """.trimIndent()

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
        val requestWithoutPassword = """
            {
                "email": "customer@example.com"
            }
        """.trimIndent()

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
        val shortPasswordRequest = """
            {
                "email": "customer@example.com",
                "password": "short"
            }
        """.trimIndent()

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(shortPasswordRequest))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }
}