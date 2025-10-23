package platform.ecommerce.dto.response

import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus
import java.time.LocalDateTime
import java.util.*

/**
 * Response DTO for member information.
 * Excludes sensitive data like password hash.
 */
data class MemberResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val role: MemberRole,
    val status: MemberStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
