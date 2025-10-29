package platform.ecommerce.config.security

import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import platform.ecommerce.security.JwtUtil
import platform.ecommerce.utils.Logger

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: UserDetailsService
): OncePerRequestFilter() {

    companion object {
        private val AUTHORIZATION_HEADER = "Authorization"
        private val BEARER_PREFIX = "Bearer"

    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = extractJwtFromRequest(request)

            if (jwt != null && isAuthenticationRequired()) {
                authenticateUser(jwt, request)
            }
        } catch (e: JwtException) {
            Logger.logger.error { "JWT authentication failed: ${e.message}" }
        } catch (e: Exception) {
            Logger.logger.error(e) { "Unexpected error during JWT authentication: ${e.message}" }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)

        return if (!bearerToken.isNullOrBlank() && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }

    private fun isAuthenticationRequired(): Boolean {
        return SecurityContextHolder.getContextHolderStrategy().context.authentication == null
    }

    private fun authenticateUser(jwt: String, request: HttpServletRequest) {
        val userEmail = jwtUtil.extractUsername(jwt)

        if (userEmail.isBlank()) {
            Logger.logger.warn { "JWT token does not contain username" }
            return
        }

        val userDetails = userDetailsService.loadUserByUsername(userEmail)

        if (jwtUtil.validateToken(jwt, userDetails)) {
            setAuthentication(userDetails, request)
            Logger.logger.debug { "Successfully authenticated user: $userEmail" }
        } else {
            Logger.logger.warn { "JWT token validation failed for user: $userEmail" }
        }
    }

    private fun setAuthentication(userDetails: UserDetails, request: HttpServletRequest) {
        val authToken = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )

        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContextHolderStrategy().context.authentication = authToken
    }
}