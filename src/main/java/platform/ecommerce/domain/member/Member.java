package platform.ecommerce.domain.member;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;
import platform.ecommerce.domain.common.SoftDeletable;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Member aggregate root.
 * Encapsulates member business logic including status transitions and address management.
 */
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity implements SoftDeletable {

    private static final int MAX_ADDRESS_COUNT = 10;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberAddress> addresses = new ArrayList<>();

    @Builder
    public Member(String email, String password, String name, String phone) {
        validateRequired(email, "email");
        validateRequired(password, "password");
        validateRequired(name, "name");

        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = MemberRole.CUSTOMER;
        this.status = MemberStatus.PENDING;
        this.emailVerified = false;
    }

    private static void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    // ========== Email Verification ==========

    /**
     * Verify email and activate account.
     * @throws InvalidStateException if already verified
     */
    public void verifyEmail() {
        if (this.emailVerified) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Email is already verified");
        }
        this.emailVerified = true;
        this.status = MemberStatus.ACTIVE;
    }

    // ========== Status Transitions ==========

    /**
     * Activate the member account.
     * @throws InvalidStateException if already withdrawn
     */
    public void activate() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new InvalidStateException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * Suspend the member account.
     * @throws InvalidStateException if already withdrawn
     */
    public void suspend() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new InvalidStateException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
        this.status = MemberStatus.SUSPENDED;
    }

    /**
     * Update login timestamp.
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    // ========== Profile Management ==========

    /**
     * Update member profile.
     * Name is only updated if not null and not blank.
     */
    public void updateProfile(String name, String phone, String profileImage) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.phone = phone;
        this.profileImage = profileImage;
    }

    /**
     * Change password.
     */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // ========== Role Management ==========

    /**
     * Upgrade to seller role.
     * @throws InvalidStateException if admin
     */
    public void upgradeToSeller() {
        if (this.role == MemberRole.ADMIN) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Admin cannot become seller");
        }
        this.role = MemberRole.SELLER;
    }

    /**
     * Check if member can login.
     * Only ACTIVE status can login.
     */
    public boolean canLogin() {
        return this.status.canLogin() && !isDeleted();
    }

    // ========== Address Management ==========

    /**
     * Add a new address.
     * First address becomes default automatically.
     * @throws InvalidStateException if address limit exceeded
     */
    public MemberAddress addAddress(String addressName, String recipientName, String recipientPhone,
                                    String zipCode, String address, String addressDetail, boolean isDefault) {
        if (this.addresses.size() >= MAX_ADDRESS_COUNT) {
            throw new InvalidStateException(ErrorCode.MEMBER_ADDRESS_LIMIT_EXCEEDED);
        }

        // If setting as default, unset all existing defaults
        if (isDefault) {
            this.addresses.forEach(a -> a.setDefault(false));
        }

        // First address is always default
        boolean shouldBeDefault = isDefault || this.addresses.isEmpty();

        MemberAddress memberAddress = MemberAddress.builder()
                .member(this)
                .name(addressName)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .zipCode(zipCode)
                .address(address)
                .addressDetail(addressDetail)
                .isDefault(shouldBeDefault)
                .build();

        this.addresses.add(memberAddress);
        return memberAddress;
    }

    /**
     * Remove an address.
     * If removed address was default, next address becomes default.
     * @throws InvalidStateException if address not found
     */
    public void removeAddress(Long addressId) {
        MemberAddress address = this.addresses.stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new InvalidStateException(ErrorCode.MEMBER_ADDRESS_NOT_FOUND));

        boolean wasDefault = address.isDefault();
        this.addresses.remove(address);

        // If removed address was default, set next as default
        if (wasDefault && !this.addresses.isEmpty()) {
            this.addresses.get(0).setDefault(true);
        }
    }

    /**
     * Get default address.
     */
    public MemberAddress getDefaultAddress() {
        return this.addresses.stream()
                .filter(MemberAddress::isDefault)
                .findFirst()
                .orElse(null);
    }

    /**
     * Find address by ID.
     * @throws InvalidStateException if address not found
     */
    public MemberAddress findAddressById(Long addressId) {
        return this.addresses.stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new InvalidStateException(ErrorCode.MEMBER_ADDRESS_NOT_FOUND));
    }

    /**
     * Set an address as default.
     * @throws InvalidStateException if address not found
     */
    public MemberAddress setDefaultAddress(Long addressId) {
        MemberAddress targetAddress = findAddressById(addressId);

        // Unset all defaults
        this.addresses.forEach(a -> a.setDefault(false));
        // Set target as default
        targetAddress.setDefault(true);

        return targetAddress;
    }

    // ========== SoftDeletable Implementation ==========

    @Override
    public void delete() {
        this.status = MemberStatus.WITHDRAWN;
        this.deletedAt = LocalDateTime.now();
    }

    @Override
    public void restore() {
        if (this.status != MemberStatus.WITHDRAWN) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Member is not withdrawn");
        }
        this.status = MemberStatus.PENDING;
        this.deletedAt = null;
    }
}
