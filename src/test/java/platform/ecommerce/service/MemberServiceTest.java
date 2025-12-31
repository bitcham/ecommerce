package platform.ecommerce.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.domain.member.MemberStatus;
import platform.ecommerce.dto.request.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.exception.DuplicateResourceException;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.InvalidStateException;
import platform.ecommerce.fixture.MemberFixture;
import platform.ecommerce.mapper.MemberMapper;
import platform.ecommerce.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * MemberService unit tests.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService Tests")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Nested
    @DisplayName("Register Member")
    class RegisterMember {

        @Test
        @DisplayName("Should register member successfully with valid request")
        void register_withValidRequest_shouldSucceed() {
            // given
            MemberCreateRequest request = MemberCreateRequest.builder()
                    .email("test@example.com")
                    .password("Password1!")
                    .passwordConfirm("Password1!")
                    .name("TestUser")
                    .phone("010-1234-5678")
                    .build();

            Member member = MemberFixture.createPendingMember();
            MemberResponse expectedResponse = MemberResponse.builder()
                    .id(1L)
                    .email("test@example.com")
                    .name("TestUser")
                    .status(MemberStatus.PENDING)
                    .build();

            given(memberRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(memberRepository.save(any(Member.class))).willReturn(member);
            given(memberMapper.toResponse(any(Member.class))).willReturn(expectedResponse);

            // when
            MemberResponse result = memberService.register(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo("test@example.com");
            verify(memberRepository).existsByEmail("test@example.com");
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("Should throw exception when password confirmation does not match")
        void register_withMismatchedPassword_shouldThrowException() {
            // given
            MemberCreateRequest request = MemberCreateRequest.builder()
                    .email("test@example.com")
                    .password("Password1!")
                    .passwordConfirm("DifferentPassword1!")
                    .name("TestUser")
                    .build();

            // when & then
            assertThatThrownBy(() -> memberService.register(request))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_withDuplicateEmail_shouldThrowException() {
            // given
            MemberCreateRequest request = MemberCreateRequest.builder()
                    .email("existing@example.com")
                    .password("Password1!")
                    .passwordConfirm("Password1!")
                    .name("TestUser")
                    .build();

            given(memberRepository.existsByEmail("existing@example.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.register(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Get Member")
    class GetMember {

        @Test
        @DisplayName("Should return member when found")
        void getMember_withValidId_shouldReturnMember() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createActiveMember();
            MemberResponse expectedResponse = MemberResponse.builder()
                    .id(memberId)
                    .email("test@example.com")
                    .name("TestUser")
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberMapper.toResponse(member)).willReturn(expectedResponse);

            // when
            MemberResponse result = memberService.getMember(memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("Should throw exception when member not found")
        void getMember_withInvalidId_shouldThrowException() {
            // given
            Long memberId = 999L;
            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.getMember(memberId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Search Members")
    class SearchMembers {

        @Test
        @DisplayName("Should return paged members")
        void searchMembers_withCondition_shouldReturnPagedResult() {
            // given
            MemberSearchCondition condition = MemberSearchCondition.empty();
            Pageable pageable = PageRequest.of(0, 10);

            Member member = MemberFixture.createActiveMember();
            Page<Member> memberPage = new PageImpl<>(List.of(member), pageable, 1);
            MemberResponse memberResponse = MemberResponse.builder()
                    .id(1L)
                    .email("test@example.com")
                    .build();

            given(memberRepository.searchMembers(condition, pageable)).willReturn(memberPage);
            given(memberMapper.toResponse(member)).willReturn(memberResponse);

            // when
            PageResponse<MemberResponse> result = memberService.searchMembers(condition, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getPage().getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Update Profile")
    class UpdateProfile {

        @Test
        @DisplayName("Should update profile successfully")
        void updateProfile_withValidRequest_shouldSucceed() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createActiveMember();
            MemberUpdateRequest request = MemberUpdateRequest.builder()
                    .name("UpdatedName")
                    .phone("010-9999-8888")
                    .build();
            MemberResponse expectedResponse = MemberResponse.builder()
                    .id(memberId)
                    .name("UpdatedName")
                    .phone("010-9999-8888")
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberMapper.toResponse(member)).willReturn(expectedResponse);

            // when
            MemberResponse result = memberService.updateProfile(memberId, request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("UpdatedName");
        }
    }

    @Nested
    @DisplayName("Change Password")
    class ChangePassword {

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_withValidRequest_shouldSucceed() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createActiveMember();
            PasswordChangeRequest request = PasswordChangeRequest.builder()
                    .currentPassword("CurrentPassword1!")
                    .newPassword("NewPassword1!")
                    .newPasswordConfirm("NewPassword1!")
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(passwordEncoder.matches("CurrentPassword1!", member.getPassword())).willReturn(true);
            given(passwordEncoder.encode("NewPassword1!")).willReturn("encodedNewPassword");

            // when
            memberService.changePassword(memberId, request);

            // then
            verify(passwordEncoder).encode("NewPassword1!");
        }

        @Test
        @DisplayName("Should throw exception when new password confirmation does not match")
        void changePassword_withMismatchedNewPassword_shouldThrowException() {
            // given
            Long memberId = 1L;
            PasswordChangeRequest request = PasswordChangeRequest.builder()
                    .currentPassword("CurrentPassword1!")
                    .newPassword("NewPassword1!")
                    .newPasswordConfirm("DifferentPassword1!")
                    .build();

            // when & then
            assertThatThrownBy(() -> memberService.changePassword(memberId, request))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("Should throw exception when current password is incorrect")
        void changePassword_withWrongCurrentPassword_shouldThrowException() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createActiveMember();
            PasswordChangeRequest request = PasswordChangeRequest.builder()
                    .currentPassword("WrongPassword1!")
                    .newPassword("NewPassword1!")
                    .newPasswordConfirm("NewPassword1!")
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(passwordEncoder.matches("WrongPassword1!", member.getPassword())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> memberService.changePassword(memberId, request))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("Withdraw Member")
    class WithdrawMember {

        @Test
        @DisplayName("Should withdraw member successfully")
        void withdraw_withValidId_shouldSucceed() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createActiveMember();
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            memberService.withdraw(memberId);

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
            assertThat(member.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("Restore Member")
    class RestoreMember {

        @Test
        @DisplayName("Should restore withdrawn member successfully")
        void restore_withWithdrawnMember_shouldSucceed() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createWithdrawnMember();
            MemberResponse expectedResponse = MemberResponse.builder()
                    .id(memberId)
                    .status(MemberStatus.PENDING)
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberMapper.toResponse(member)).willReturn(expectedResponse);

            // when
            MemberResponse result = memberService.restore(memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
            assertThat(member.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when restoring non-withdrawn member")
        void restore_withActiveMember_shouldThrowException() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createActiveMember();
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> memberService.restore(memberId))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("Address Management")
    class AddressManagement {

        @Test
        @DisplayName("Should add address successfully")
        void addAddress_withValidRequest_shouldSucceed() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createActiveMember();
            AddressCreateRequest request = AddressCreateRequest.builder()
                    .name("Home")
                    .recipientName("John Doe")
                    .recipientPhone("010-1234-5678")
                    .zipCode("12345")
                    .address("123 Main St")
                    .addressDetail("Apt 101")
                    .isDefault(true)
                    .build();
            AddressResponse expectedResponse = AddressResponse.builder()
                    .id(1L)
                    .name("Home")
                    .isDefault(true)
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberMapper.toAddressResponse(any())).willReturn(expectedResponse);

            // when
            AddressResponse result = memberService.addAddress(memberId, request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Home");
            assertThat(member.getAddresses()).hasSize(1);
        }

        @Test
        @DisplayName("Should remove address successfully")
        void removeAddress_withValidIds_shouldSucceed() {
            // given
            Long memberId = 1L;
            Long addressId = 100L;
            Member member = MemberFixture.createActiveMember();
            member.addAddress("Home", "John Doe", "010-1234-5678",
                    "12345", "123 Main St", "Apt 101", true);

            // Set ID for unit test (JPA doesn't assign ID without persistence)
            org.springframework.test.util.ReflectionTestUtils.setField(
                    member.getAddresses().get(0), "id", addressId);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            memberService.removeAddress(memberId, addressId);

            // then
            assertThat(member.getAddresses()).isEmpty();
        }
    }
}
