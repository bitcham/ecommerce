package platform.ecommerce.dto.request.coupon;

import lombok.Builder;

@Builder
public record CouponSearchCondition(
        Boolean activeOnly,
        String codeContains
) {
}
