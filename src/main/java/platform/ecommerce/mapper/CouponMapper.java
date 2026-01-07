package platform.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import platform.ecommerce.domain.coupon.Coupon;
import platform.ecommerce.domain.coupon.MemberCoupon;
import platform.ecommerce.dto.response.coupon.CouponResponse;
import platform.ecommerce.dto.response.coupon.MemberCouponResponse;

import java.util.List;

/**
 * MapStruct mapper for Coupon entity.
 */
@Mapper(componentModel = "spring")
public interface CouponMapper {

    @Mapping(target = "remainingQuantity", expression = "java(coupon.getRemainingQuantity())")
    @Mapping(target = "active", expression = "java(coupon.isActive())")
    CouponResponse toResponse(Coupon coupon);

    List<CouponResponse> toResponseList(List<Coupon> coupons);

    @Mapping(target = "coupon", source = "coupon")
    @Mapping(target = "used", expression = "java(memberCoupon.isUsed())")
    @Mapping(target = "available", expression = "java(memberCoupon.isAvailable())")
    @Mapping(target = "expiresAt", source = "coupon.validTo")
    MemberCouponResponse toMemberCouponResponse(MemberCoupon memberCoupon);

    List<MemberCouponResponse> toMemberCouponResponseList(List<MemberCoupon> memberCoupons);
}
