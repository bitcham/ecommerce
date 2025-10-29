package platform.ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.InstanceOfAssertFactories.LIST
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import platform.ecommerce.dto.request.LoginRequest
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.dto.response.LoginResponse
import platform.ecommerce.dto.response.MemberResponse
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus
import platform.ecommerce.exception.DuplicateEmailException
import platform.ecommerce.exception.InvalidCredentialsException
import platform.ecommerce.security.JwtUtil
import platform.ecommerce.service.AuthService
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(
    controllers = [AuthController::class],
    excludeAutoConfiguration = [org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class]
)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvcTester: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var authService: AuthService

    @MockitoBean
    private lateinit var user: UserDetailsService

    @MockitoBean
    private lateinit var jwtUtil: JwtUtil

    @Test
    fun `Should return 201 and member response when registration is successful`() {
        // Given
        val request = MemberRegister(
            email = "test@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe",
            phone = "1234567890"
        )

        val response = MemberResponse(
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

        whenever(authService.register(any())).thenReturn(response)

        // When & Then
        assertThat(
            mockMvcTester.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .hasStatus(HttpStatus.CREATED)
            .hasContentType(MediaType.APPLICATION_JSON)
            .bodyJson()
            .hasPathSatisfying("$.success") { success -> assertThat(success).isEqualTo(true) }
            .hasPathSatisfying("$.message") { message -> assertThat(message).isEqualTo("Member registered successfully") }
            .hasPathSatisfying("$.data.email") { email -> assertThat(email).isEqualTo(request.email) }
            .hasPathSatisfying("$.data.firstName") { firstName -> assertThat(firstName).isEqualTo(request.firstName) }
            .hasPathSatisfying("$.data.lastName") { lastName -> assertThat(lastName).isEqualTo(request.lastName) }
            .hasPathSatisfying("$.data.phone") { phone -> assertThat(phone).isEqualTo(request.phone) }
            .hasPathSatisfying("$.data.role") { role -> assertThat(role).isEqualTo("CUSTOMER") }
            .hasPathSatisfying("$.data.status") { status -> assertThat(status).isEqualTo("PENDING") }
    }

    @Test
    fun `Should return 201 when registering without optional phone`() {
        // Given
        val request = MemberRegister(
            email = "test@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe",
            phone = null
        )

        val response = MemberResponse(
            id = UUID.randomUUID(),
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            phone = null,
            role = MemberRole.CUSTOMER,
            status = MemberStatus.PENDING,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        whenever(authService.register(any())).thenReturn(response)

        // When & Then
        assertThat(
            mockMvcTester.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .hasPathSatisfying("$.success") { success -> assertThat(success).isEqualTo(true) }
            .hasPathSatisfying("$.data.phone") { phone -> assertThat(phone).isNull() }
    }

    @Test
    fun `Should return 400 when email is invalid`() {
        // Given
        val invalidRequest = """
            {
                "email": "not-an-email",
                "password": "password123",
                "firstName": "John",
                "lastName": "Doe"
            }
        """.trimIndent()

        // When & Then
        assertThat(
            mockMvcTester.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying("$.success") { success -> assertThat(success).isEqualTo(false) }
            .hasPathSatisfying("$.message") { message -> assertThat(message).isEqualTo("Validation failed") }
            .hasPathSatisfying("$.errors") { errors -> assertThat(errors).asInstanceOf(LIST).isNotEmpty }
    }

    @Test
    fun `Should return 400 when password is too short`() {
        // Given
        val invalidRequest = """
            {
                "email": "test@example.com",
                "password": "short",
                "firstName": "John",
                "lastName": "Doe"
            }
        """.trimIndent()

        // When & Then
        assertThat(
            mockMvcTester.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying("$.success") { success -> assertThat(success).isEqualTo(false) }
            .hasPathSatisfying("$.message") { message -> assertThat(message).isEqualTo("Validation failed") }
            .hasPathSatisfying("$.errors") { errors -> assertThat(errors).asInstanceOf(LIST).isNotEmpty }
    }

    @Test
    fun `Should return 400 when required fields are missing`() {
        // Given
        val invalidRequest = """
            {
                "email": "test@example.com",
                "password": "password123"
            }
        """.trimIndent()

        // When & Then
        assertThat(
            mockMvcTester.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying("$.success") { success -> assertThat(success).isEqualTo(false) }
            .hasPathSatisfying("$.message") { message -> assertThat(message).isEqualTo("Missing required fields in request body") }
    }

    @Test
    fun `Should return 400 when all fields are blank`() {
        // Given
        val invalidRequest = """
            {
                "email": "",
                "password": "",
                "firstName": "",
                "lastName": ""
            }
        """.trimIndent()

        // When & Then
        assertThat(
            mockMvcTester.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying("$.success") { success -> assertThat(success).isEqualTo(false) }
            .hasPathSatisfying("$.errors") { errors -> assertThat(errors).asInstanceOf(LIST).hasSizeGreaterThanOrEqualTo(4) }
    }

    @Test
    fun `Should return 409 when email already exists`() {
        // Given
        val request = MemberRegister(
            email = "existing@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe"
        )

        whenever(authService.register(any()))
            .thenThrow(DuplicateEmailException("Email already exists: ${request.email}"))

        // When & Then
        assertThat(
            mockMvcTester.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson()
            .hasPathSatisfying("$.success") { success -> assertThat(success).isEqualTo(false) }
            .hasPathSatisfying("$.message") { message ->
                assertThat(message).asString().contains("Email already exists: existing@example.com")
            }
    }

    @Test
    fun `Should return 200 and tokens when login is successful`() {
        // Given
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )

        val memberId = UUID.randomUUID()
        val response = LoginResponse(
            id = memberId,
            email = request.email,
            role = "CUSTOMER",
            accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.access",
            refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.refresh",
            tokenType = "Bearer",
            expiresIn = 3600000L
        )

        whenever(authService.login(any())).thenReturn(response)

        // When & Then
        assertThat(
            mockMvcTester.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .hasStatus(HttpStatus.OK)
            .hasContentType(MediaType.APPLICATION_JSON)
            .bodyJson()
            .hasPathSatisfying("$.success") { success -> assertThat(success).isEqualTo(true) }
            .hasPathSatisfying("$.message") { message -> assertThat(message).isEqualTo("Successfully logged in") }
            .hasPathSatisfying("$.data.id") { id -> assertThat(id).isEqualTo(memberId.toString()) }
            .hasPathSatisfying("$.data.email") { email -> assertThat(email).isEqualTo(request.email) }
            .hasPathSatisfying("$.data.role") { role -> assertThat(role).isEqualTo("CUSTOMER") }
            .hasPathSatisfying("$.data.accessToken") { token -> assertThat(token).asString().startsWith("eyJ") }
            .hasPathSatisfying("$.data.refreshToken") { token -> assertThat(token).asString().startsWith("eyJ") }
            .hasPathSatisfying("$.data.tokenType") { type -> assertThat(type).isEqualTo("Bearer") }
            .hasPathSatisfying("$.data.expiresIn") { expires -> assertThat(expires).isEqualTo(3600000) }
    }

    @Test
    fun `Should return 401 when credentials are invalid`() {
        // Given
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        whenever(authService.login(any()))
            .thenThrow(InvalidCredentialsException("Invalid username or password"))

        // When & Then
        assertThat(
            mockMvcTester.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .hasStatus(HttpStatus.UNAUTHORIZED)
            .bodyJson()
            .hasPathSatisfying("$.success") { success -> assertThat(success).isEqualTo(false) }
            .hasPathSatisfying("$.message") { message ->
                assertThat(message).asString().contains("Invalid username or password")
            }
    }

    @Test
    fun `Should return 400 when login email format is invalid`() {
        // Given
        val invalidRequest = """
            {
                "email": "not-an-email",
                "password": "password123"
            }
        """.trimIndent()

        // When & Then
        assertThat(
            mockMvcTester.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying("$.success") { success -> assertThat(success).isEqualTo(false) }
            .hasPathSatisfying("$.message") { message -> assertThat(message).isEqualTo("Validation failed") }
            .hasPathSatisfying("$.errors") { errors -> assertThat(errors).asInstanceOf(LIST).isNotEmpty }
    }

    @Test
    fun `Should return 400 when login password is too short`() {
        // Given
        val invalidRequest = """
            {
                "email": "test@example.com",
                "password": "short"
            }
        """.trimIndent()

        // When & Then
        assertThat(
            mockMvcTester.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying("$.success") { success -> assertThat(success).isEqualTo(false) }
            .hasPathSatisfying("$.message") { message -> assertThat(message).isEqualTo("Validation failed") }
    }

    @Test
    fun `Should return 400 when login credentials are missing`() {
        // Given
        val invalidRequest = """
            {
                "email": "",
                "password": ""
            }
        """.trimIndent()

        // When & Then
        assertThat(
            mockMvcTester.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying("$.success") { success -> assertThat(success).isEqualTo(false) }
            .hasPathSatisfying("$.errors") { errors -> assertThat(errors).asInstanceOf(LIST).hasSizeGreaterThanOrEqualTo(2) }
    }
}
