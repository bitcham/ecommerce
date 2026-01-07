package platform.ecommerce.repository.coupon;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.coupon.MemberCoupon;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MemberCoupon entity.
 * Note: Coupon's @SQLRestriction automatically filters deleted coupons in JOIN queries.
 */
public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    @Query("SELECT mc FROM MemberCoupon mc JOIN FETCH mc.coupon WHERE mc.memberId = :memberId AND mc.used = false")
    List<MemberCoupon> findAvailableByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT mc FROM MemberCoupon mc JOIN FETCH mc.coupon WHERE mc.memberId = :memberId AND mc.used = false AND mc.coupon.validTo > CURRENT_TIMESTAMP")
    Page<MemberCoupon> findAvailableByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT mc FROM MemberCoupon mc JOIN FETCH mc.coupon WHERE mc.memberId = :memberId")
    List<MemberCoupon> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT mc FROM MemberCoupon mc JOIN FETCH mc.coupon WHERE mc.memberId = :memberId")
    Page<MemberCoupon> findAllByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT mc FROM MemberCoupon mc JOIN FETCH mc.coupon WHERE mc.id = :id")
    Optional<MemberCoupon> findByIdWithCoupon(@Param("id") Long id);

    boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);
}
