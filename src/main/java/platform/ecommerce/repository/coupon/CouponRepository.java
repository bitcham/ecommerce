package platform.ecommerce.repository.coupon;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.coupon.Coupon;
import platform.ecommerce.dto.request.coupon.CouponSearchCondition;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Coupon aggregate root.
 */
public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryCustom {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT c FROM Coupon c WHERE c.active = true AND c.validTo > CURRENT_TIMESTAMP")
    List<Coupon> findAllActive();

    @Query("SELECT c FROM Coupon c WHERE c.deletedAt IS NULL")
    Page<Coupon> findAllNotDeleted(Pageable pageable);
}
