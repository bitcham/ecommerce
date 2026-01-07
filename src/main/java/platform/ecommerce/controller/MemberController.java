package platform.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.dto.request.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.service.application.MemberApplicationService;

/**
 * Member REST controller.
 */
@Tag(name = "Member", description = "Member management API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberApplicationService memberApplicationService;

    @Operation(summary = "Register member", description = "Register a new member")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> register(
            @Valid @RequestBody MemberCreateRequest request
    ) {
        MemberResponse response = memberApplicationService.register(request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Search members", description = "Search members with conditions")
    @GetMapping
    public ApiResponse<PageResponse<MemberResponse>> searchMembers(
            @Parameter(description = "Email search keyword") @RequestParam(required = false) String email,
            @Parameter(description = "Name search keyword") @RequestParam(required = false) String name,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        MemberSearchCondition condition = MemberSearchCondition.builder()
                .email(email)
                .name(name)
                .excludeWithdrawn(true)
                .build();

        PageResponse<MemberResponse> response = memberApplicationService.searchMembers(condition, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get member detail", description = "Get member detail by ID")
    @GetMapping("/{memberId}")
    public ApiResponse<MemberDetailResponse> getMember(
            @Parameter(description = "Member ID") @PathVariable Long memberId
    ) {
        MemberDetailResponse response = memberApplicationService.getMemberDetail(memberId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Update profile", description = "Update member profile information")
    @PatchMapping("/{memberId}")
    public ApiResponse<MemberResponse> updateProfile(
            @Parameter(description = "Member ID") @PathVariable Long memberId,
            @Valid @RequestBody MemberUpdateRequest request
    ) {
        MemberResponse response = memberApplicationService.updateProfile(memberId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Change password", description = "Change member password")
    @PatchMapping("/{memberId}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Parameter(description = "Member ID") @PathVariable Long memberId,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        memberApplicationService.changePassword(memberId, request);
    }

    @Operation(summary = "Withdraw member", description = "Withdraw member account (soft delete)")
    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(
            @Parameter(description = "Member ID") @PathVariable Long memberId
    ) {
        memberApplicationService.withdraw(memberId);
    }

    @Operation(summary = "Restore member", description = "Restore withdrawn member account")
    @PostMapping("/{memberId}/restore")
    public ApiResponse<MemberResponse> restore(
            @Parameter(description = "Member ID") @PathVariable Long memberId
    ) {
        MemberResponse response = memberApplicationService.restore(memberId);
        return ApiResponse.success(response);
    }

    // ========== Address Endpoints ==========

    @Operation(summary = "Add address", description = "Add a new address for member")
    @PostMapping("/{memberId}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressResponse> addAddress(
            @Parameter(description = "Member ID") @PathVariable Long memberId,
            @Valid @RequestBody AddressCreateRequest request
    ) {
        AddressResponse response = memberApplicationService.addAddress(memberId, request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Update address", description = "Update member address information")
    @PatchMapping("/{memberId}/addresses/{addressId}")
    public ApiResponse<AddressResponse> updateAddress(
            @Parameter(description = "Member ID") @PathVariable Long memberId,
            @Parameter(description = "Address ID") @PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateRequest request
    ) {
        AddressResponse response = memberApplicationService.updateAddress(memberId, addressId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Remove address", description = "Remove member address")
    @DeleteMapping("/{memberId}/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAddress(
            @Parameter(description = "Member ID") @PathVariable Long memberId,
            @Parameter(description = "Address ID") @PathVariable Long addressId
    ) {
        memberApplicationService.removeAddress(memberId, addressId);
    }

    @Operation(summary = "Set default address", description = "Set address as default")
    @PostMapping("/{memberId}/addresses/{addressId}/default")
    public ApiResponse<AddressResponse> setDefaultAddress(
            @Parameter(description = "Member ID") @PathVariable Long memberId,
            @Parameter(description = "Address ID") @PathVariable Long addressId
    ) {
        AddressResponse response = memberApplicationService.setDefaultAddress(memberId, addressId);
        return ApiResponse.success(response);
    }
}
