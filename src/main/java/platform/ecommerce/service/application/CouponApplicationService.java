package platform.ecommerce.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import platform.ecommerce.dto.request.coupon.CouponCreateRequest;
import platform.ecommerce.dto.request.coupon.CouponSearchCondition;
import platform.ecommerce.dto.request.coupon.CouponUpdateRequest;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.coupon.CouponApplyResponse;
import platform.ecommerce.dto.response.coupon.CouponCalculationResponse;
import platform.ecommerce.dto.response.coupon.CouponResponse;
import platform.ecommerce.dto.response.coupon.MemberCouponResponse;
import platform.ecommerce.service.coupon.CouponService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Coupon application service.
 * Currently delegates to CouponService.
 */
@Service
@RequiredArgsConstructor
public class CouponApplicationService {

    private final CouponService couponService;

    public CouponResponse createCoupon(CouponCreateRequest request) {
        return couponService.createCoupon(request);
    }

    public CouponResponse getCoupon(Long couponId) {
        return couponService.getCoupon(couponId);
    }

    public CouponResponse getCouponByCode(String code) {
        return couponService.getCouponByCode(code);
    }

    public PageResponse<CouponResponse> searchCoupons(CouponSearchCondition condition, Pageable pageable) {
        return couponService.searchCoupons(condition, pageable);
    }

    public CouponResponse updateCoupon(Long couponId, CouponUpdateRequest request) {
        return couponService.updateCoupon(couponId, request);
    }

    public void deactivateCoupon(Long couponId) {
        couponService.deactivateCoupon(couponId);
    }

    public void deleteCoupon(Long couponId) {
        couponService.deleteCoupon(couponId);
    }

    public MemberCouponResponse issueCoupon(Long couponId, Long memberId) {
        return couponService.issueCoupon(couponId, memberId);
    }

    public MemberCouponResponse issueCouponByCode(String code, Long memberId) {
        return couponService.issueCouponByCode(code, memberId);
    }

    public PageResponse<MemberCouponResponse> getMemberCoupons(Long memberId, boolean availableOnly, Pageable pageable) {
        return couponService.getMemberCoupons(memberId, availableOnly, pageable);
    }

    public List<MemberCouponResponse> getMemberCoupons(Long memberId) {
        return couponService.getMemberCoupons(memberId);
    }

    public CouponCalculationResponse calculateDiscount(Long couponId, BigDecimal orderAmount) {
        return couponService.calculateDiscount(couponId, orderAmount);
    }

    public PageResponse<MemberCouponResponse> getAvailableCouponsForOrder(Long memberId, BigDecimal orderAmount, Pageable pageable) {
        return couponService.getAvailableCouponsForOrder(memberId, orderAmount, pageable);
    }

    public CouponApplyResponse applyCoupon(String code, BigDecimal orderAmount) {
        return couponService.applyCoupon(code, orderAmount);
    }

    public void useCoupon(Long memberCouponId, Long orderId) {
        couponService.useCoupon(memberCouponId, orderId);
    }

    public void restoreCoupon(Long memberCouponId) {
        couponService.restoreCoupon(memberCouponId);
    }
}
