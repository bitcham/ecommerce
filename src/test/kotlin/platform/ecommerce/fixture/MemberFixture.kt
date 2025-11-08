package platform.ecommerce.fixture

import platform.ecommerce.domain.Member
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.dto.response.MemberResponse
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus
import java.time.Instant
import java.util.*

object MemberFixture {

    fun createMemberRegisterRequest(
        email: String = "test@example.com",
        password: String = "password123",
        firstName: String = "John",
        lastName: String = "Doe",
        phone: String? = "010-1234-5678"
    ): MemberRegister {
        return MemberRegister(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            phone = phone
        )
    }

    fun createMember(
        id: UUID = UUID.randomUUID(),
        email: String = "test@example.com",
        password: String = "encodedPassword",
        firstName: String = "John",
        lastName: String = "Doe",
        phone: String? = "010-1234-5678",
        role: MemberRole = MemberRole.CUSTOMER,
        status: MemberStatus = MemberStatus.PENDING
    ): Member {
        return Member(
            email = email,
            passwordHash = password,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            role = role,
            status = status
        ).apply {
            // Set ID using reflection
            val idField = Member::class.java.superclass.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, id)
        }
    }

    fun createMemberResponse(
        id: UUID = UUID.randomUUID(),
        email: String = "test@example.com",
        firstName: String = "John",
        lastName: String = "Doe",
        phone: String? = "010-1234-5678",
        role: MemberRole = MemberRole.CUSTOMER,
        status: MemberStatus = MemberStatus.PENDING,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now()
    ): MemberResponse {
        return MemberResponse(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            role = role,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}