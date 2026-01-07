package platform.ecommerce.mapper;

import org.mapstruct.Mapper;
import platform.ecommerce.dto.response.LoginResponse;
import platform.ecommerce.dto.response.MemberResponse;
import platform.ecommerce.dto.response.TokenResponse;
import platform.ecommerce.service.auth.LoginResult;
import platform.ecommerce.service.auth.TokenResult;

/**
 * MapStruct mapper for Auth-related DTOs.
 */
@Mapper(componentModel = "spring", uses = {MemberMapper.class})
public interface AuthMapper {

    default TokenResponse toTokenResponse(TokenResult result) {
        return TokenResponse.of(
                result.accessToken(),
                result.refreshToken(),
                result.expiresIn()
        );
    }

    default LoginResponse toLoginResponse(LoginResult result, MemberMapper memberMapper) {
        TokenResponse tokens = TokenResponse.of(
                result.accessToken(),
                result.refreshToken(),
                result.expiresIn()
        );
        MemberResponse member = memberMapper.toResponse(result.member());
        return LoginResponse.of(tokens, member);
    }
}
