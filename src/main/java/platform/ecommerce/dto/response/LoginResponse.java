package platform.ecommerce.dto.response;

import lombok.Builder;

/**
 * Login response DTO containing tokens and member info.
 */
@Builder
public record LoginResponse(
        TokenResponse tokens,
        MemberResponse member
) {
    public static LoginResponse of(TokenResponse tokens, MemberResponse member) {
        return LoginResponse.builder()
                .tokens(tokens)
                .member(member)
                .build();
    }
}
