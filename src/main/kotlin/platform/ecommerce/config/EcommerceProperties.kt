package platform.ecommerce.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class EcommerceProperties(
    val mail: Mail,
    val url: String,
    val emailVerification: EmailVerification
) {
    data class Mail(
        val from: String
    )

    data class EmailVerification(
        val expirationHours: Long = 24
    )
}
