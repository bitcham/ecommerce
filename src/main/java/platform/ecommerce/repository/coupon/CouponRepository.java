package platform.ecommerce.repository.coupon;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.coupon.Coupon;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Coupon aggregate root.
 * Note: @SQLRestriction on Coupon entity automatically filters deleted records.
 */
public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryCustom {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT c FROM Coupon c WHERE c.active = true AND c.validTo > CURRENT_TIMESTAMP")
    List<Coupon> findAllActive();

    // ========== Admin Methods (bypass @SQLRestriction) ==========

    /**
     * Find coupon by ID including deleted (for admin).
     */
    @Query(value = "SELECT * FROM coupon WHERE id = :id", nativeQuery = true)
    Optional<Coupon> findByIdIncludingDeleted(@Param("id") Long id);

    /**
     * Find all deleted coupons (for admin).
     */
    @Query(value = "SELECT * FROM coupon WHERE deleted_at IS NOT NULL", nativeQuery = true)
    List<Coupon> findAllDeleted();
}
