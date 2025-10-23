package platform.ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.dto.response.MemberResponse
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus
import platform.ecommerce.exception.DuplicateEmailException
import platform.ecommerce.service.AuthService
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(
    controllers = [AuthController::class],
    excludeAutoConfiguration = [org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class]
)
@AutoConfigureMockMvc
@DisplayName("Authentication API")
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvcTester: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var authService: AuthService

    @Nested
    @DisplayName("POST /auth/register")
    inner class RegisterMember {

        @Test
        @DisplayName("Should return 201 and member response when registration is successful")
        fun shouldRegisterMemberSuccessfully() {
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
        @DisplayName("Should return 201 when registering without optional phone")
        fun shouldRegisterMemberWithoutPhone() {
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

        @Nested
        @DisplayName("Validation Errors")
        inner class ValidationErrors {

            @Test
            @DisplayName("Should return 400 when email is invalid")
            fun shouldRejectInvalidEmail() {
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
                    .hasPathSatisfying("$.errors") { errors -> assertThat(errors).asList().isNotEmpty }
            }

            @Test
            @DisplayName("Should return 400 when password is too short")
            fun shouldRejectShortPassword() {
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
                    .hasPathSatisfying("$.errors") { errors -> assertThat(errors).asList().isNotEmpty }
            }

            @Test
            @DisplayName("Should return 400 when required fields are missing")
            fun shouldRejectMissingFields() {
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
                    .hasPathSatisfying("$.message") { message -> assertThat(message).isEqualTo("Malformed JSON request") }
            }

            @Test
            @DisplayName("Should return 400 when all fields are blank")
            fun shouldRejectAllBlankFields() {
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
                    .hasPathSatisfying("$.errors") { errors -> assertThat(errors).asList().hasSizeGreaterThanOrEqualTo(4) }
            }
        }

        @Nested
        @DisplayName("Business Logic Errors")
        inner class BusinessLogicErrors {

            @Test
            @DisplayName("Should return 409 when email already exists")
            fun shouldRejectDuplicateEmail() {
                // Given
                val request = MemberRegister(
                    email = "existing@example.com",
                    password = "password123",
                    firstName = "John",
                    lastName = "Doe"
                )

                whenever(authService.register(any()))
                    .thenThrow(DuplicateEmailException(request.email))

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
        }
    }
}
