package platform.ecommerce.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.domain.member.MemberAddress;
import platform.ecommerce.dto.request.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.mapper.MemberMapper;
import platform.ecommerce.service.MemberService;

/**
 * Member application service.
 * Handles DTO conversion using MemberMapper.
 */
@Service
@RequiredArgsConstructor
public class MemberApplicationService {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    /**
     * Register a new member.
     */
    public MemberResponse register(MemberCreateRequest request) {
        Member member = memberService.register(request);
        return memberMapper.toResponse(member);
    }

    /**
     * Get member by ID.
     */
    public MemberResponse getMember(Long memberId) {
        Member member = memberService.getMember(memberId);
        return memberMapper.toResponse(member);
    }

    /**
     * Get member detail with addresses.
     */
    public MemberDetailResponse getMemberDetail(Long memberId) {
        Member member = memberService.getMember(memberId);
        return memberMapper.toDetailResponse(member);
    }

    /**
     * Search members with conditions.
     */
    public PageResponse<MemberResponse> searchMembers(MemberSearchCondition condition, Pageable pageable) {
        Page<Member> memberPage = memberService.searchMembers(condition, pageable);
        Page<MemberResponse> responsePage = memberPage.map(memberMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    /**
     * Update member profile.
     */
    public MemberResponse updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = memberService.updateProfile(memberId, request);
        return memberMapper.toResponse(member);
    }

    /**
     * Change member password.
     */
    public void changePassword(Long memberId, PasswordChangeRequest request) {
        memberService.changePassword(memberId, request);
    }

    /**
     * Withdraw member (soft delete).
     */
    public void withdraw(Long memberId) {
        memberService.withdraw(memberId);
    }

    /**
     * Restore withdrawn member.
     */
    public MemberResponse restore(Long memberId) {
        Member member = memberService.restore(memberId);
        return memberMapper.toResponse(member);
    }

    /**
     * Add address for member.
     */
    public AddressResponse addAddress(Long memberId, AddressCreateRequest request) {
        MemberAddress address = memberService.addAddress(memberId, request);
        return memberMapper.toAddressResponse(address);
    }

    /**
     * Update member address.
     */
    public AddressResponse updateAddress(Long memberId, Long addressId, AddressUpdateRequest request) {
        MemberAddress address = memberService.updateAddress(memberId, addressId, request);
        return memberMapper.toAddressResponse(address);
    }

    /**
     * Remove member address.
     */
    public void removeAddress(Long memberId, Long addressId) {
        memberService.removeAddress(memberId, addressId);
    }

    /**
     * Set address as default.
     */
    public AddressResponse setDefaultAddress(Long memberId, Long addressId) {
        MemberAddress address = memberService.setDefaultAddress(memberId, addressId);
        return memberMapper.toAddressResponse(address);
    }
}
