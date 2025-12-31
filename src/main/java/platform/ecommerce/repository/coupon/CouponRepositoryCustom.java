package platform.ecommerce.repository.coupon;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import platform.ecommerce.domain.coupon.Coupon;
import platform.ecommerce.dto.request.coupon.CouponSearchCondition;

/**
 * Custom repository interface for Coupon queries.
 */
public interface CouponRepositoryCustom {

    Page<Coupon> searchCoupons(CouponSearchCondition condition, Pageable pageable);
}
