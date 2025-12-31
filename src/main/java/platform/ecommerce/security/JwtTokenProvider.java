package platform.ecommerce.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import platform.ecommerce.config.JwtProperties;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.exception.AuthenticationException;
import platform.ecommerce.exception.ErrorCode;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT token provider for creating and validating tokens.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String issuer;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = jwtProperties.getAccessTokenExpiration();
        this.refreshTokenExpiration = jwtProperties.getRefreshTokenExpiration();
        this.issuer = jwtProperties.getIssuer();
    }

    /**
     * Generate access token for member.
     */
    public String generateAccessToken(Member member) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(member.getId().toString())
                .claim("email", member.getEmail())
                .claim("role", member.getRole().name())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Get member ID from token.
     */
    public Long getMemberIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Get email from token.
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    /**
     * Get role from token.
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Validate token.
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parse and validate token.
     */
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw new AuthenticationException(ErrorCode.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw new AuthenticationException(ErrorCode.TOKEN_INVALID);
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw new AuthenticationException(ErrorCode.TOKEN_INVALID);
        } catch (SecurityException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            throw new AuthenticationException(ErrorCode.TOKEN_INVALID);
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            throw new AuthenticationException(ErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * Get access token expiration in seconds.
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }

    /**
     * Get refresh token expiration in days.
     */
    public long getRefreshTokenExpirationDays() {
        return refreshTokenExpiration / (1000 * 60 * 60 * 24);
    }
}
