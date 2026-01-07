package platform.ecommerce.service.auth;

import platform.ecommerce.domain.member.Member;

/**
 * Internal result type for login operation.
 * Contains all the data needed to construct LoginResponse in ApplicationService.
 */
public record LoginResult(
        Member member,
        String accessToken,
        String refreshToken,
        long expiresIn
) {
    public static LoginResult of(Member member, String accessToken, String refreshToken, long expiresIn) {
        return new LoginResult(member, accessToken, refreshToken, expiresIn);
    }
}
