package platform.ecommerce.domain

import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

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