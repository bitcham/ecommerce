package platform.ecommerce.domain.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.domain.member.MemberStatus;
import platform.ecommerce.exception.InvalidStateException;
import platform.ecommerce.fixture.MemberFixture;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailVerificationToken domain unit tests.
 */
@DisplayName("EmailVerificationToken Domain Tests")
class EmailVerificationTokenTest {

    @Nested
    @DisplayName("Create Token")
    class CreateToken {

        @Test
        @DisplayName("Should create token with valid member")
        void createToken_withValidMember_shouldSucceed() {
            // given
            Member member = MemberFixture.createPendingMember();

            // when
            EmailVerificationToken token = EmailVerificationToken.builder()
                    .member(member)
                    .build();

            // then
            assertThat(token.getToken()).isNotBlank();
            assertThat(token.getMember()).isEqualTo(member);
            assertThat(token.isUsed()).isFalse();
            assertThat(token.getExpiresAt()).isNotNull();
            assertThat(token.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Verify Token")
    class VerifyToken {

        @Test
        @DisplayName("Should verify token and activate member")
        void verify_withValidToken_shouldActivateMember() {
            // given
            Member member = MemberFixture.createPendingMember();
            EmailVerificationToken token = EmailVerificationToken.builder()
                    .member(member)
                    .build();

            // when
            token.verify();

            // then
            assertThat(token.isUsed()).isTrue();
            assertThat(token.getVerifiedAt()).isNotNull();
            assertThat(member.isEmailVerified()).isTrue();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw exception when verifying already used token")
        void verify_withUsedToken_shouldThrowException() {
            // given
            Member member = MemberFixture.createPendingMember();
            EmailVerificationToken token = EmailVerificationToken.builder()
                    .member(member)
                    .build();
            token.verify();

            // when & then
            assertThatThrownBy(token::verify)
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("Token Validity")
    class TokenValidity {

        @Test
        @DisplayName("Should return false for used token")
        void isValid_withUsedToken_shouldReturnFalse() {
            // given
            Member member = MemberFixture.createPendingMember();
            EmailVerificationToken token = EmailVerificationToken.builder()
                    .member(member)
                    .build();
            token.verify();

            // when & then
            assertThat(token.isValid()).isFalse();
        }
    }
}
