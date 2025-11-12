package platform.ecommerce.dto.response

import java.util.*

data class LoginResponse(
    val id: UUID,
    val email: String,
    val role: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long
) {
}