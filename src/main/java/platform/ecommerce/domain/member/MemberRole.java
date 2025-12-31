package platform.ecommerce.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Member role enumeration.
 */
@Getter
@RequiredArgsConstructor
public enum MemberRole {

    CUSTOMER("Customer"),
    SELLER("Seller"),
    ADMIN("Administrator");

    private final String description;

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isSeller() {
        return this == SELLER;
    }

    public boolean isCustomer() {
        return this == CUSTOMER;
    }
}
