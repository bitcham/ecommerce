package platform.ecommerce.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import platform.ecommerce.config.EcommerceProperties
import platform.ecommerce.utils.Logger.Companion.logger
import java.util.UUID

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val ecommerceProperties: EcommerceProperties
) {

    fun sendVerificationEmail(recipientEmail: String, token: UUID) {
        val verificationLink = buildVerificationLink(token)

        val message = SimpleMailMessage().apply {
            from = ecommerceProperties.mail.from
            setTo(recipientEmail)
            subject = "Please verify your email address"
            text = """
                Welcome to our E-Commerce Platform!

                Please click the link below to verify your email address:
                $verificationLink

                This link will expire in ${ecommerceProperties.emailVerification.expirationHours} hours.

                If you did not create an account, please ignore this email.
            """.trimIndent()
        }

        try {
            mailSender.send(message)
            logger.info { "Verification email sent to $recipientEmail" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send verification email to $recipientEmail" }
            throw e
        }
    }

    private fun buildVerificationLink(token: UUID): String {
        return "${ecommerceProperties.url}/api/auth/verify-email?token=$token"
    }
}