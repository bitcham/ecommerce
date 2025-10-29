package platform.ecommerce.security

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import platform.ecommerce.config.JwtProperties
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil(
    private val jwtProperties: JwtProperties
) {
    private val signingKey: SecretKey

    init {
        log.info { "Initializing JWT Provider..." }

        val keyBytes = jwtProperties.secret.toByteArray(Charsets.UTF_8)
        val keyLengthBits = keyBytes.size * 8

        require(keyBytes.size >= 32) {
            buildString {
                appendLine("JWT secret key validation failed:")
                appendLine("  Current: ${keyBytes.size} bytes ($keyLengthBits bits)")
                appendLine("  Required: 32 bytes (256 bits)")
                appendLine("  Solution: openssl rand -base64 32")
            }
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes)

        log.info { "JWT Provider initialized. Key strength: $keyLengthBits bits" }
    }

    fun generateAccessToken(userDetails: UserDetails): String {
        val claims = buildClaims(userDetails)
        claims["type"] = "access"
        return createToken(claims, userDetails.username, jwtProperties.expiration)
    }

    fun generateRefreshToken(userDetails: UserDetails): String {
        val claims = mutableMapOf<String, Any>("type" to "refresh")
        return createToken(claims, userDetails.username, jwtProperties.refreshExpiration)
    }

    fun getAccessTokenExpiration(): Long{
        return jwtProperties.expiration
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        return try {
            val username = extractUsername(token)
            username == userDetails.username && !isTokenExpired(token)
        } catch (e: ExpiredJwtException) {
            log.debug { "Token expired for user: ${e.claims.subject}" }
            false
        } catch (e: JwtException) {
            log.warn { "Invalid token: ${e.message}" }
            false
        } catch (e: Exception) {
            log.error(e) { "Unexpected error during token validation" }
            false
        }
    }

    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    fun extractAuthorities(token: String): List<String> {
        return try {
            val claims = extractAllClaims(token)
            @Suppress("UNCHECKED_CAST")
            claims["authorities"] as? List<String> ?: emptyList()
        } catch (e: Exception) {
            log.warn { "Failed to extract authorities from token: ${e.message}" }
            emptyList()
        }
    }

    private fun buildClaims(userDetails: UserDetails): MutableMap<String, Any> {
        return mutableMapOf(
            "authorities" to userDetails.authorities.map(GrantedAuthority::getAuthority)
        )
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    private fun createToken(
        claims: Map<String, Any>,
        subject: String,
        expirationTime: Long
    ): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(Date(now))
            .expiration(Date(now + expirationTime))
            .signWith(signingKey)
            .compact()
    }

    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
