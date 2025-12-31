package platform.ecommerce.dto.response.coupon;

import java.math.BigDecimal;

public record CouponCalculationResponse(
        Long couponId,
        String couponCode,
        String couponName,
        BigDecimal orderAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        boolean applicable,
        String message
) {
    public static CouponCalculationResponse applicable(
            Long couponId,
            String couponCode,
            String couponName,
            BigDecimal orderAmount,
            BigDecimal discountAmount
    ) {
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);
        return new CouponCalculationResponse(
                couponId, couponCode, couponName, orderAmount, discountAmount, finalAmount, true, null
        );
    }

    public static CouponCalculationResponse notApplicable(
            Long couponId,
            String couponCode,
            String couponName,
            BigDecimal orderAmount,
            String message
    ) {
        return new CouponCalculationResponse(
                couponId, couponCode, couponName, orderAmount, BigDecimal.ZERO, orderAmount, false, message
        );
    }
}
