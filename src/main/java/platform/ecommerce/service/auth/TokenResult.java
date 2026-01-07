package platform.ecommerce.service.auth;

/**
 * Internal result type for token operations.
 * Contains data needed to construct TokenResponse in ApplicationService.
 */
public record TokenResult(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
    public static TokenResult of(String accessToken, String refreshToken, long expiresIn) {
        return new TokenResult(accessToken, refreshToken, expiresIn);
    }
}
