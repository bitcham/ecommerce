package platform.ecommerce.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import platform.ecommerce.config.EcommerceProperties
import platform.ecommerce.domain.EmailVerificationToken
import platform.ecommerce.domain.Member
import platform.ecommerce.exception.TokenAlreadyUsedException
import platform.ecommerce.exception.TokenExpiredException
import platform.ecommerce.exception.TokenNotFoundException
import platform.ecommerce.repository.EmailVerificationTokenRepository
import java.time.Instant
import java.util.UUID

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val memberService: MemberService,
    private val ecommerceProperties: EcommerceProperties
) {

    @Transactional
    fun createVerificationToken(member: Member): EmailVerificationToken {
        val expirationSeconds = ecommerceProperties.emailVerification.expirationHours * 60 * 60
        val token = EmailVerificationToken(
            token = UUID.randomUUID(),
            member = member,
            expiresAt = Instant.now().plusSeconds(expirationSeconds)
        )
        return emailVerificationTokenRepository.save(token)
    }

    @Transactional
    fun verifyEmail(token: UUID) {
        val foundToken = emailVerificationTokenRepository.findByToken(token)?:
        throw TokenNotFoundException("Token not found")

        if(foundToken.verified){
            throw TokenAlreadyUsedException("Token already used")
        }

        if(foundToken.expiresAt.isBefore(Instant.now())) {
            throw TokenExpiredException("Token expired")
        }

        memberService.activate(foundToken.member)

        foundToken.verified = true
    }


}
