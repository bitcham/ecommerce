package platform.ecommerce.repository

import org.springframework.data.jpa.repository.JpaRepository
import platform.ecommerce.domain.EmailVerificationToken
import java.util.UUID

interface EmailVerificationTokenRepository: JpaRepository<EmailVerificationToken, Long> {
    fun findByToken(token: UUID): EmailVerificationToken?
}