package platform.ecommerce.dto.request.coupon;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponUpdateRequest(
        @Size(min = 2, max = 100)
        String name,

        @Size(max = 500)
        String description,

        @Min(0)
        BigDecimal minOrderAmount,

        @Min(0)
        BigDecimal maxDiscountAmount,

        LocalDateTime validFrom,

        LocalDateTime validUntil,

        @Min(0)
        Integer maxUsageCount,

        @Min(0)
        Integer maxUsagePerMember
) {
}
