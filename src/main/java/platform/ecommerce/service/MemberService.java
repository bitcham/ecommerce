package platform.ecommerce.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.domain.member.MemberAddress;
import platform.ecommerce.dto.request.*;

/**
 * Member domain service interface.
 * Returns entities for ApplicationService to convert to DTOs.
 */
public interface MemberService {

    /**
     * Register a new member.
     * @return created Member entity
     */
    Member register(MemberCreateRequest request);

    /**
     * Get member by ID.
     * @return Member entity
     */
    Member getMember(Long memberId);

    /**
     * Search members with conditions.
     * @return page of Member entities
     */
    Page<Member> searchMembers(MemberSearchCondition condition, Pageable pageable);

    /**
     * Update member profile.
     * @return updated Member entity
     */
    Member updateProfile(Long memberId, MemberUpdateRequest request);

    /**
     * Change member password.
     */
    void changePassword(Long memberId, PasswordChangeRequest request);

    /**
     * Withdraw member (soft delete).
     */
    void withdraw(Long memberId);

    /**
     * Restore withdrawn member.
     * @return restored Member entity
     */
    Member restore(Long memberId);

    /**
     * Add address for member.
     * @return created MemberAddress entity
     */
    MemberAddress addAddress(Long memberId, AddressCreateRequest request);

    /**
     * Update member address.
     * @return updated MemberAddress entity
     */
    MemberAddress updateAddress(Long memberId, Long addressId, AddressUpdateRequest request);

    /**
     * Remove member address.
     */
    void removeAddress(Long memberId, Long addressId);

    /**
     * Set address as default.
     * @return updated MemberAddress entity
     */
    MemberAddress setDefaultAddress(Long memberId, Long addressId);
}
