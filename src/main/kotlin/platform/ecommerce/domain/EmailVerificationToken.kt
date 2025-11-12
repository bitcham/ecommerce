package platform.ecommerce.domain

import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY
import java.time.Instant
import java.util.*

@Entity
@Table(name = "email_verification_token")
class EmailVerificationToken(
    val token: UUID,

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    val member: Member,

    val expiresAt: Instant,

    var verified: Boolean = false
): BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}