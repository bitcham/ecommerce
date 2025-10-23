package platform.ecommerce.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import platform.ecommerce.domain.Member
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.dto.response.MemberResponse
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus
import platform.ecommerce.mapper.MemberMapper
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var memberMapper: MemberMapper

    @InjectMocks
    private lateinit var authService: AuthService

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

        val member = Member.register(
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

}
