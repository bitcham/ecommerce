package platform.ecommerce.service.auth;

import platform.ecommerce.domain.member.Member;

/**
 * Internal result type for member registration.
 * Contains Member entity and verification token for ApplicationService to send email.
 */
public record RegistrationResult(
        Member member,
        String verificationToken
) {
    public static RegistrationResult of(Member member, String verificationToken) {
        return new RegistrationResult(member, verificationToken);
    }
}
