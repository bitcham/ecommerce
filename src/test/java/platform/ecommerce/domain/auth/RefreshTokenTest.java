package platform.ecommerce.domain.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.fixture.MemberFixture;

import static org.assertj.core.api.Assertions.*;

/**
 * RefreshToken domain unit tests.
 */
@DisplayName("RefreshToken Domain Tests")
class RefreshTokenTest {

    @Nested
    @DisplayName("Create Token")
    class CreateToken {

        @Test
        @DisplayName("Should create token with valid member")
        void createToken_withValidMember_shouldSucceed() {
            // given
            Member member = MemberFixture.createActiveMember();

            // when
            RefreshToken token = RefreshToken.builder()
                    .member(member)
                    .expirationDays(7)
                    .deviceInfo("Test Browser")
                    .ipAddress("127.0.0.1")
                    .build();

            // then
            assertThat(token.getToken()).isNotBlank();
            assertThat(token.getMember()).isEqualTo(member);
            assertThat(token.isRevoked()).isFalse();
            assertThat(token.getExpiresAt()).isNotNull();
            assertThat(token.getDeviceInfo()).isEqualTo("Test Browser");
            assertThat(token.getIpAddress()).isEqualTo("127.0.0.1");
            assertThat(token.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Token Operations")
    class TokenOperations {

        @Test
        @DisplayName("Should revoke token")
        void revoke_shouldSetRevokedTrue() {
            // given
            Member member = MemberFixture.createActiveMember();
            RefreshToken token = RefreshToken.builder()
                    .member(member)
                    .expirationDays(7)
                    .build();

            // when
            token.revoke();

            // then
            assertThat(token.isRevoked()).isTrue();
            assertThat(token.isValid()).isFalse();
        }

        @Test
        @DisplayName("Should rotate token")
        void rotate_shouldGenerateNewToken() {
            // given
            Member member = MemberFixture.createActiveMember();
            RefreshToken token = RefreshToken.builder()
                    .member(member)
                    .expirationDays(7)
                    .build();
            String originalToken = token.getToken();

            // when
            String newToken = token.rotate(7);

            // then
            assertThat(newToken).isNotEqualTo(originalToken);
            assertThat(token.getToken()).isEqualTo(newToken);
        }
    }

    @Nested
    @DisplayName("Token Validity")
    class TokenValidity {

        @Test
        @DisplayName("Should return false for revoked token")
        void isValid_withRevokedToken_shouldReturnFalse() {
            // given
            Member member = MemberFixture.createActiveMember();
            RefreshToken token = RefreshToken.builder()
                    .member(member)
                    .expirationDays(7)
                    .build();
            token.revoke();

            // when & then
            assertThat(token.isValid()).isFalse();
        }
    }
}
