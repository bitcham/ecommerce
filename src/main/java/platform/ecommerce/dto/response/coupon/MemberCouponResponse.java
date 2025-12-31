package platform.ecommerce.dto.response.coupon;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Member coupon response DTO.
 */
@Builder
public record MemberCouponResponse(
        Long id,
        CouponResponse coupon,
        boolean used,
        LocalDateTime usedAt,
        boolean available,
        LocalDateTime expiresAt
) {
}
