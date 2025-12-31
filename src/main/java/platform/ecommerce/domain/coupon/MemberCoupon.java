package platform.ecommerce.domain.coupon;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;

import java.time.LocalDateTime;

/**
 * Member coupon entity.
 * Represents a coupon issued to a specific member.
 */
@Entity
@Table(name = "member_coupon", indexes = {
        @Index(name = "idx_member_coupon_member", columnList = "member_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCoupon extends BaseEntity {

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "order_id")
    private Long orderId;

    @Builder
    public MemberCoupon(Long memberId, Coupon coupon) {
        this.memberId = memberId;
        this.coupon = coupon;
        this.used = false;
    }

    /**
     * Use this coupon for an order.
     */
    public void use(Long orderId) {
        if (this.used) {
            throw new InvalidStateException(ErrorCode.COUPON_ALREADY_USED);
        }
        if (!coupon.isValid()) {
            throw new InvalidStateException(ErrorCode.COUPON_EXPIRED);
        }
        this.used = true;
        this.usedAt = LocalDateTime.now();
        this.orderId = orderId;
        this.coupon.use();
    }

    /**
     * Restore coupon (e.g., when order is cancelled).
     */
    public void restore() {
        if (this.used) {
            this.used = false;
            this.usedAt = null;
            this.orderId = null;
            this.coupon.restoreQuantity();
        }
    }

    /**
     * Check if this coupon is available for use.
     */
    public boolean isAvailable() {
        return !used && coupon.isValid();
    }

    /**
     * Check if this coupon has expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(coupon.getValidTo());
    }
}
