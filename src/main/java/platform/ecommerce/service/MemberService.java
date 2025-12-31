package platform.ecommerce.service;

import org.springframework.data.domain.Pageable;
import platform.ecommerce.dto.request.*;
import platform.ecommerce.dto.response.*;

/**
 * Member service interface.
 */
public interface MemberService {

    /**
     * Register a new member.
     *
     * @param request registration request
     * @return created member response
     */
    MemberResponse register(MemberCreateRequest request);

    /**
     * Get member by ID.
     *
     * @param memberId member ID
     * @return member response
     */
    MemberResponse getMember(Long memberId);

    /**
     * Get member detail with addresses.
     *
     * @param memberId member ID
     * @return member detail response
     */
    MemberDetailResponse getMemberDetail(Long memberId);

    /**
     * Search members with conditions.
     *
     * @param condition search condition
     * @param pageable  pagination info
     * @return page of member responses
     */
    PageResponse<MemberResponse> searchMembers(MemberSearchCondition condition, Pageable pageable);

    /**
     * Update member profile.
     *
     * @param memberId member ID
     * @param request  update request
     * @return updated member response
     */
    MemberResponse updateProfile(Long memberId, MemberUpdateRequest request);

    /**
     * Change member password.
     *
     * @param memberId member ID
     * @param request  password change request
     */
    void changePassword(Long memberId, PasswordChangeRequest request);

    /**
     * Withdraw member (soft delete).
     *
     * @param memberId member ID
     */
    void withdraw(Long memberId);

    /**
     * Restore withdrawn member.
     *
     * @param memberId member ID
     * @return restored member response
     */
    MemberResponse restore(Long memberId);

    /**
     * Add address for member.
     *
     * @param memberId member ID
     * @param request  address create request
     * @return created address response
     */
    AddressResponse addAddress(Long memberId, AddressCreateRequest request);

    /**
     * Update member address.
     *
     * @param memberId  member ID
     * @param addressId address ID
     * @param request   address update request
     * @return updated address response
     */
    AddressResponse updateAddress(Long memberId, Long addressId, AddressUpdateRequest request);

    /**
     * Remove member address.
     *
     * @param memberId  member ID
     * @param addressId address ID
     */
    void removeAddress(Long memberId, Long addressId);

    /**
     * Set address as default.
     *
     * @param memberId  member ID
     * @param addressId address ID
     * @return updated address response
     */
    AddressResponse setDefaultAddress(Long memberId, Long addressId);
}
