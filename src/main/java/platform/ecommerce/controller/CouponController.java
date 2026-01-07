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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.dto.request.coupon.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.dto.response.coupon.*;
import platform.ecommerce.security.SecurityUtils;
import platform.ecommerce.service.application.CouponApplicationService;

import java.math.BigDecimal;

/**
 * Coupon REST controller.
 */
@Tag(name = "Coupon", description = "Coupon management API")
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponApplicationService couponApplicationService;

    // ========== Admin Endpoints ==========

    @Operation(summary = "Create coupon", description = "Create a new coupon (admin)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponResponse> createCoupon(
            @Valid @RequestBody CouponCreateRequest request
    ) {
        CouponResponse response = couponApplicationService.createCoupon(request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Get coupon", description = "Get coupon by ID")
    @GetMapping("/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponResponse> getCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long couponId
    ) {
        CouponResponse response = couponApplicationService.getCoupon(couponId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get coupon by code", description = "Get coupon by code")
    @GetMapping("/code/{code}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CouponResponse> getCouponByCode(
            @Parameter(description = "Coupon code") @PathVariable String code
    ) {
        CouponResponse response = couponApplicationService.getCouponByCode(code);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Search coupons", description = "Search coupons with conditions (admin)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<CouponResponse>> searchCoupons(
            @Parameter(description = "Active only") @RequestParam(required = false) Boolean activeOnly,
            @Parameter(description = "Code contains") @RequestParam(required = false) String codeContains,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        CouponSearchCondition condition = CouponSearchCondition.builder()
                .activeOnly(activeOnly)
                .codeContains(codeContains)
                .build();
        PageResponse<CouponResponse> response = couponApplicationService.searchCoupons(condition, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Update coupon", description = "Update coupon information")
    @PatchMapping("/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponResponse> updateCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long couponId,
            @Valid @RequestBody CouponUpdateRequest request
    ) {
        CouponResponse response = couponApplicationService.updateCoupon(couponId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Deactivate coupon", description = "Deactivate a coupon")
    @PostMapping("/{couponId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long couponId
    ) {
        couponApplicationService.deactivateCoupon(couponId);
    }

    @Operation(summary = "Delete coupon", description = "Soft delete a coupon")
    @DeleteMapping("/{couponId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long couponId
    ) {
        couponApplicationService.deleteCoupon(couponId);
    }

    // ========== Member Endpoints ==========

    @Operation(summary = "Get my coupons", description = "Get coupons for authenticated member")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<MemberCouponResponse>> getMyCoupons(
            @Parameter(description = "Available only") @RequestParam(defaultValue = "false") boolean availableOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PageResponse<MemberCouponResponse> response = couponApplicationService.getMemberCoupons(memberId, availableOnly, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Issue coupon", description = "Issue coupon to member")
    @PostMapping("/{couponId}/issue")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MemberCouponResponse> issueCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long couponId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        MemberCouponResponse response = couponApplicationService.issueCoupon(couponId, memberId);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Issue coupon by code", description = "Issue coupon to member by code")
    @PostMapping("/issue-by-code")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MemberCouponResponse> issueCouponByCode(
            @Parameter(description = "Coupon code") @RequestParam String code
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        MemberCouponResponse response = couponApplicationService.issueCouponByCode(code, memberId);
        return ApiResponse.created(response);
    }

    // ========== Calculation Endpoints ==========

    @Operation(summary = "Calculate discount", description = "Calculate discount amount for order")
    @GetMapping("/{couponId}/calculate")
    public ApiResponse<CouponCalculationResponse> calculateDiscount(
            @Parameter(description = "Coupon ID") @PathVariable Long couponId,
            @Parameter(description = "Order amount") @RequestParam BigDecimal orderAmount
    ) {
        CouponCalculationResponse response = couponApplicationService.calculateDiscount(couponId, orderAmount);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get available coupons", description = "Get coupons applicable to order amount")
    @GetMapping("/available")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<MemberCouponResponse>> getAvailableCoupons(
            @Parameter(description = "Order amount") @RequestParam BigDecimal orderAmount,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PageResponse<MemberCouponResponse> response = couponApplicationService.getAvailableCouponsForOrder(memberId, orderAmount, pageable);
        return ApiResponse.success(response);
    }
}
