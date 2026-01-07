package platform.ecommerce.domain.coupon;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import platform.ecommerce.domain.common.BaseEntity;
import platform.ecommerce.domain.common.SoftDeletable;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Coupon aggregate root.
 * Represents a discount coupon with usage rules.
 */
@Entity
@Table(name = "coupon", indexes = {
        @Index(name = "idx_coupon_code", columnList = "code")
})
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity implements SoftDeletable {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponType type;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "minimum_order", precision = 12, scale = 2)
    private BigDecimal minimumOrder;

    @Column(name = "maximum_discount", precision = 12, scale = 2)
    private BigDecimal maximumDiscount;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "used_quantity", nullable = false)
    private int usedQuantity;

    @Column(nullable = false)
    private boolean active;

    @Builder
    public Coupon(String code, String name, CouponType type, BigDecimal discountValue,
                  BigDecimal minimumOrder, BigDecimal maximumDiscount,
                  LocalDateTime validFrom, LocalDateTime validTo, int totalQuantity) {
        validateDiscountValue(discountValue, type);
        validateDateRange(validFrom, validTo);

        this.code = code.toUpperCase();
        this.name = name;
        this.type = type;
        this.discountValue = discountValue;
        this.minimumOrder = minimumOrder != null ? minimumOrder : BigDecimal.ZERO;
        this.maximumDiscount = maximumDiscount;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.totalQuantity = totalQuantity;
        this.usedQuantity = 0;
        this.active = true;
    }

    /**
     * Calculate discount amount for given order amount.
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isApplicable(orderAmount)) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (type == CouponType.PERCENTAGE) {
            discount = orderAmount.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);

            // Apply maximum discount cap
            if (maximumDiscount != null && discount.compareTo(maximumDiscount) > 0) {
                discount = maximumDiscount;
            }
        } else {
            discount = discountValue;
        }

        // Discount cannot exceed order amount
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    /**
     * Check if coupon is applicable to order amount.
     */
    public boolean isApplicable(BigDecimal orderAmount) {
        return isValid() && orderAmount.compareTo(minimumOrder) >= 0;
    }

    /**
     * Check if coupon is currently valid.
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return active
                && now.isAfter(validFrom)
                && now.isBefore(validTo)
                && hasQuantityAvailable();
    }

    /**
     * Check if quantity is available.
     */
    public boolean hasQuantityAvailable() {
        return usedQuantity < totalQuantity;
    }

    /**
     * Get remaining quantity.
     */
    public int getRemainingQuantity() {
        return totalQuantity - usedQuantity;
    }

    /**
     * Use one coupon (increment used count).
     */
    public void use() {
        if (!hasQuantityAvailable()) {
            throw new InvalidStateException(ErrorCode.COUPON_LIMIT_EXCEEDED);
        }
        this.usedQuantity++;
    }

    /**
     * Restore one coupon quantity (decrement used count, e.g., on order cancel).
     */
    public void restoreQuantity() {
        if (this.usedQuantity > 0) {
            this.usedQuantity--;
        }
    }

    /**
     * Activate coupon.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Deactivate coupon.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Update coupon name.
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * Update minimum order amount.
     */
    public void updateMinimumOrder(BigDecimal minimumOrder) {
        this.minimumOrder = minimumOrder != null ? minimumOrder : BigDecimal.ZERO;
    }

    /**
     * Update maximum discount amount.
     */
    public void updateMaximumDiscount(BigDecimal maximumDiscount) {
        this.maximumDiscount = maximumDiscount;
    }

    /**
     * Update valid period.
     */
    public void updateValidPeriod(LocalDateTime validFrom, LocalDateTime validTo) {
        validateDateRange(validFrom, validTo);
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    // ========== SoftDeletable Implementation ==========

    @Override
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.active = false;
    }

    @Override
    public void restore() {
        this.deletedAt = null;
    }

    @Override
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // ========== Validation ==========

    private void validateDiscountValue(BigDecimal discountValue, CouponType type) {
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount value must be positive");
        }
        if (type == CouponType.PERCENTAGE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100%");
        }
    }

    private void validateDateRange(LocalDateTime validFrom, LocalDateTime validTo) {
        if (validFrom == null || validTo == null) {
            throw new IllegalArgumentException("Valid from and to dates are required");
        }
        if (validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("Valid to date must be after valid from date");
        }
    }
}
