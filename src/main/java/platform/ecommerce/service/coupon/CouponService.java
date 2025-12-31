package platform.ecommerce.service.coupon;

import org.springframework.data.domain.Pageable;
import platform.ecommerce.dto.request.coupon.CouponCreateRequest;
import platform.ecommerce.dto.request.coupon.CouponSearchCondition;
import platform.ecommerce.dto.request.coupon.CouponUpdateRequest;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.coupon.CouponApplyResponse;
import platform.ecommerce.dto.response.coupon.CouponCalculationResponse;
import platform.ecommerce.dto.response.coupon.CouponResponse;
import platform.ecommerce.dto.response.coupon.MemberCouponResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Coupon operations.
 */
public interface CouponService {

    /**
     * Creates a new coupon.
     */
    CouponResponse createCoupon(CouponCreateRequest request);

    /**
     * Gets a coupon by ID.
     */
    CouponResponse getCoupon(Long couponId);

    /**
     * Gets a coupon by code.
     */
    CouponResponse getCouponByCode(String code);

    /**
     * Searches coupons with conditions.
     */
    PageResponse<CouponResponse> searchCoupons(CouponSearchCondition condition, Pageable pageable);

    /**
     * Updates a coupon.
     */
    CouponResponse updateCoupon(Long couponId, CouponUpdateRequest request);

    /**
     * Deactivates a coupon.
     */
    void deactivateCoupon(Long couponId);

    /**
     * Deletes a coupon.
     */
    void deleteCoupon(Long couponId);

    /**
     * Issues a coupon to a member by coupon ID.
     */
    MemberCouponResponse issueCoupon(Long couponId, Long memberId);

    /**
     * Issues a coupon to a member by code.
     */
    MemberCouponResponse issueCouponByCode(String code, Long memberId);

    /**
     * Issues a coupon to a member.
     */
    MemberCouponResponse issueCouponToMember(Long couponId, Long memberId);

    /**
     * Gets a member's coupons with pagination.
     */
    PageResponse<MemberCouponResponse> getMemberCoupons(Long memberId, boolean availableOnly, Pageable pageable);

    /**
     * Gets a member's coupons.
     */
    List<MemberCouponResponse> getMemberCoupons(Long memberId);

    /**
     * Gets a member's available (unused) coupons.
     */
    List<MemberCouponResponse> getAvailableMemberCoupons(Long memberId);

    /**
     * Calculates discount for a coupon.
     */
    CouponCalculationResponse calculateDiscount(Long couponId, BigDecimal orderAmount);

    /**
     * Gets available coupons for an order amount.
     */
    PageResponse<MemberCouponResponse> getAvailableCouponsForOrder(Long memberId, BigDecimal orderAmount, Pageable pageable);

    /**
     * Applies a coupon to calculate discount.
     */
    CouponApplyResponse applyCoupon(String code, BigDecimal orderAmount);

    /**
     * Uses a member coupon for an order.
     */
    void useCoupon(Long memberCouponId, Long orderId);

    /**
     * Restores a coupon (e.g., when order is cancelled).
     */
    void restoreCoupon(Long memberCouponId);

    /**
     * Validates if a coupon can be applied.
     */
    boolean validateCoupon(String code, BigDecimal orderAmount);
}
