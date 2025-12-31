package platform.ecommerce.dto.response.coupon;

import lombok.Builder;
import platform.ecommerce.domain.coupon.CouponType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Coupon response DTO.
 */
@Builder
public record CouponResponse(
        Long id,
        String code,
        String name,
        CouponType type,
        BigDecimal discountValue,
        BigDecimal minimumOrder,
        BigDecimal maximumDiscount,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        int totalQuantity,
        int usedQuantity,
        int remainingQuantity,
        boolean active
) {
}
