package platform.ecommerce.fixture;

import org.springframework.test.util.ReflectionTestUtils;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.domain.member.MemberRole;

/**
 * Test fixture for Member entity.
 * Provides factory methods for creating test instances.
 */
public class MemberFixture {

    public static final String DEFAULT_EMAIL = "test@example.com";
    public static final String DEFAULT_PASSWORD = "encodedPassword123";
    public static final String DEFAULT_NAME = "TestUser";
    public static final String DEFAULT_PHONE = "010-1234-5678";

    public static Member createPendingMember() {
        return Member.builder()
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .name(DEFAULT_NAME)
                .phone(DEFAULT_PHONE)
                .build();
    }

    public static Member createPendingMember(String email) {
        return Member.builder()
                .email(email)
                .password(DEFAULT_PASSWORD)
                .name(DEFAULT_NAME)
                .phone(DEFAULT_PHONE)
                .build();
    }

    public static Member createActiveMember() {
        Member member = createPendingMember();
        member.verifyEmail();
        return member;
    }

    public static Member createActiveMember(String email) {
        Member member = createPendingMember(email);
        member.verifyEmail();
        return member;
    }

    public static Member createSuspendedMember() {
        Member member = createActiveMember();
        member.suspend();
        return member;
    }

    public static Member createWithdrawnMember() {
        Member member = createActiveMember();
        member.delete();
        return member;
    }

    public static Member createSellerMember() {
        Member member = createActiveMember();
        member.upgradeToSeller();
        return member;
    }

    /**
     * Creates an active member with ADMIN role.
     * Uses ReflectionTestUtils since admin promotion is not exposed in domain.
     */
    public static Member createAdminMember() {
        Member member = createActiveMember();
        ReflectionTestUtils.setField(member, "role", MemberRole.ADMIN);
        return member;
    }
}
