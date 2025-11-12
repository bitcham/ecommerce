package platform.ecommerce.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.Repository
import platform.ecommerce.domain.EmailVerificationToken
import java.util.*

interface EmailVerificationTokenRepository: Repository<EmailVerificationToken, Long> {
    fun findByToken(token: UUID): EmailVerificationToken?
    fun save(token: EmailVerificationToken): EmailVerificationToken
}