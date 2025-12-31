# Coupon Module Implementation Plan

## Overview
Coupon system for discounts with various types, validity periods, and usage limits.

## Domain Design

### Entities

#### Coupon (Aggregate Root)
```java
@Entity
public class Coupon extends BaseEntity {
    private String code;              // Unique coupon code
    private String name;              // Display name
    private CouponType type;          // PERCENTAGE, FIXED_AMOUNT
    private BigDecimal discountValue; // Discount amount or percentage
    private BigDecimal minimumOrder;  // Minimum order amount required
    private BigDecimal maximumDiscount; // Cap for percentage discounts
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private int totalQuantity;        // Total coupons available
    private int usedQuantity;         // Coupons used
    private boolean active;

    // Operations
    calculateDiscount(orderAmount)
    use() / restore()
    isValid()
}
```

#### CouponType (Enum)
```java
public enum CouponType {
    PERCENTAGE,   // e.g., 10% off
    FIXED_AMOUNT  // e.g., ₩5000 off
}
```

#### MemberCoupon (Issued coupon)
```java
@Entity
public class MemberCoupon extends BaseEntity {
    private Long memberId;
    private Coupon coupon;
    private boolean used;
    private LocalDateTime usedAt;

    // Operations
    use(orderId)
}
```

### Business Rules
1. Coupon code must be unique
2. Percentage discount capped by maximumDiscount
3. Fixed amount cannot exceed order total
4. Valid within date range
5. Quantity limit enforcement
6. One coupon per order (future enhancement for stacking)

## Service Layer

### CouponService
- createCoupon(request): Create new coupon
- getCoupon(couponId): Get coupon details
- getCouponByCode(code): Get by code
- issueCouponToMember(couponId, memberId): Issue to member
- getMemberCoupons(memberId): Get member's coupons
- applyCoupon(code, orderAmount): Calculate discount
- useCoupon(memberCouponId): Mark as used
- validateCoupon(code, orderAmount): Check if valid

## DTOs

### Request
- CouponCreateRequest(code, name, type, discountValue, minimumOrder, maximumDiscount, validFrom, validTo, quantity)

### Response
- CouponResponse(id, code, name, type, discountValue, minimumOrder, maximumDiscount, validFrom, validTo, remaining)
- MemberCouponResponse(id, coupon, used, usedAt, expiresAt)
- CouponApplyResponse(discountAmount, finalAmount)
