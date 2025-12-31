package platform.ecommerce.dto.response.coupon;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Response DTO for coupon application result.
 */
@Builder
public record CouponApplyResponse(
        String code,
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount
) {
}
