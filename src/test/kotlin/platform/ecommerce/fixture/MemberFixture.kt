package platform.ecommerce.fixture

import platform.ecommerce.domain.Member
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus
import java.util.*

object MemberFixture {
    fun createMember(
        id: UUID = UUID.randomUUID(),
        email: String = "test@example.com",
        passwordHash: String = "hashedPassword",
        firstName: String = "John",
        lastName: String = "Doe",
        phone: String? = null,
        role: MemberRole = MemberRole.CUSTOMER,
        status: MemberStatus = MemberStatus.PENDING
    ): Member {
        return Member(
            email = email,
            passwordHash = passwordHash,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            role = role,
            status = status
        ).apply {
            val idField = Member::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, id)
        }
    }
}
