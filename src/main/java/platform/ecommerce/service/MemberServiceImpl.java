package platform.ecommerce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.domain.member.MemberAddress;
import platform.ecommerce.dto.request.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.exception.DuplicateResourceException;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;
import platform.ecommerce.mapper.MemberMapper;
import platform.ecommerce.repository.MemberRepository;

/**
 * Member service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberResponse register(MemberCreateRequest request) {
        log.info("Registering new member with email: {}", request.email());

        validatePasswordMatch(request);
        validateEmailNotExists(request.email());

        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("Member registered successfully: id={}", savedMember.getId());

        return memberMapper.toResponse(savedMember);
    }

    @Override
    public MemberResponse getMember(Long memberId) {
        Member member = findMemberById(memberId);
        return memberMapper.toResponse(member);
    }

    @Override
    public MemberDetailResponse getMemberDetail(Long memberId) {
        Member member = findMemberById(memberId);
        return memberMapper.toDetailResponse(member);
    }

    @Override
    public PageResponse<MemberResponse> searchMembers(MemberSearchCondition condition, Pageable pageable) {
        Page<Member> memberPage = memberRepository.searchMembers(condition, pageable);
        Page<MemberResponse> responsePage = memberPage.map(memberMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional
    public MemberResponse updateProfile(Long memberId, MemberUpdateRequest request) {
        log.info("Updating profile for member: id={}", memberId);

        Member member = findMemberById(memberId);
        member.updateProfile(request.name(), request.phone(), request.profileImage());

        return memberMapper.toResponse(member);
    }

    @Override
    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequest request) {
        log.info("Changing password for member: id={}", memberId);

        validateNewPasswordMatch(request);

        Member member = findMemberById(memberId);
        validateCurrentPassword(member, request.currentPassword());

        member.changePassword(passwordEncoder.encode(request.newPassword()));
        log.info("Password changed successfully for member: id={}", memberId);
    }

    @Override
    @Transactional
    public void withdraw(Long memberId) {
        log.info("Withdrawing member: id={}", memberId);

        Member member = findMemberById(memberId);
        member.delete();

        log.info("Member withdrawn successfully: id={}", memberId);
    }

    @Override
    @Transactional
    public MemberResponse restore(Long memberId) {
        log.info("Restoring member: id={}", memberId);

        Member member = findMemberById(memberId);
        member.restore();

        log.info("Member restored successfully: id={}", memberId);
        return memberMapper.toResponse(member);
    }

    @Override
    @Transactional
    public AddressResponse addAddress(Long memberId, AddressCreateRequest request) {
        log.info("Adding address for member: id={}", memberId);

        Member member = findMemberById(memberId);
        MemberAddress address = member.addAddress(
                request.name(),
                request.recipientName(),
                request.recipientPhone(),
                request.zipCode(),
                request.address(),
                request.addressDetail(),
                request.isDefault()
        );

        log.info("Address added successfully for member: id={}, addressId={}", memberId, address.getId());
        return memberMapper.toAddressResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long memberId, Long addressId, AddressUpdateRequest request) {
        log.info("Updating address for member: id={}, addressId={}", memberId, addressId);

        Member member = findMemberById(memberId);
        MemberAddress address = member.findAddressById(addressId);

        address.update(
                request.name(),
                request.recipientName(),
                request.recipientPhone(),
                request.zipCode(),
                request.address(),
                request.addressDetail()
        );

        return memberMapper.toAddressResponse(address);
    }

    @Override
    @Transactional
    public void removeAddress(Long memberId, Long addressId) {
        log.info("Removing address for member: id={}, addressId={}", memberId, addressId);

        Member member = findMemberById(memberId);
        member.removeAddress(addressId);

        log.info("Address removed successfully for member: id={}, addressId={}", memberId, addressId);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long memberId, Long addressId) {
        log.info("Setting default address for member: id={}, addressId={}", memberId, addressId);

        Member member = findMemberById(memberId);
        MemberAddress address = member.setDefaultAddress(addressId);

        return memberMapper.toAddressResponse(address);
    }

    // ========== Private Helper Methods ==========

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validatePasswordMatch(MemberCreateRequest request) {
        if (!request.isPasswordMatched()) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Passwords do not match");
        }
    }

    private void validateNewPasswordMatch(PasswordChangeRequest request) {
        if (!request.isNewPasswordMatched()) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "New passwords do not match");
        }
    }

    private void validateEmailNotExists(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(ErrorCode.MEMBER_EMAIL_DUPLICATED);
        }
    }

    private void validateCurrentPassword(Member member, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Current password is incorrect");
        }
    }
}
