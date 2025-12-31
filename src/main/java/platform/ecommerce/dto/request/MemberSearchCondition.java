package platform.ecommerce.dto.request;

import lombok.Builder;
import platform.ecommerce.domain.member.MemberRole;
import platform.ecommerce.domain.member.MemberStatus;

/**
 * Member search condition for dynamic queries.
 */
@Builder
public record MemberSearchCondition(
        String email,
        String name,
        MemberStatus status,
        MemberRole role,
        Boolean excludeWithdrawn
) {
    public MemberSearchCondition {
        // Default to true if not specified
        if (excludeWithdrawn == null) {
            excludeWithdrawn = true;
        }
    }

    public static MemberSearchCondition empty() {
        return MemberSearchCondition.builder().build();
    }
}
