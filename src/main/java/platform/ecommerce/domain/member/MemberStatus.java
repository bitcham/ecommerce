package platform.ecommerce.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Member account status enumeration.
 */
@Getter
@RequiredArgsConstructor
public enum MemberStatus {

    PENDING("Pending Email Verification"),
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    WITHDRAWN("Withdrawn");

    private final String description;

    public boolean canLogin() {
        return this == ACTIVE;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    public boolean isWithdrawn() {
        return this == WITHDRAWN;
    }
}
