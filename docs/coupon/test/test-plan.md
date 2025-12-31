# Coupon Module Test Plan

## Domain Tests (CouponTest.java)

### Coupon Creation
- [ ] should create percentage coupon
- [ ] should create fixed amount coupon
- [ ] should throw exception for invalid discount value

### Discount Calculation
- [ ] should calculate percentage discount correctly
- [ ] should cap percentage discount at maximum
- [ ] should calculate fixed discount correctly
- [ ] should not exceed order amount for fixed discount
- [ ] should return zero if below minimum order

### Validation
- [ ] should be valid within date range
- [ ] should be invalid before validFrom
- [ ] should be invalid after validTo
- [ ] should be invalid when quantity exhausted
- [ ] should be invalid when deactivated

### Usage
- [ ] should increment used quantity
- [ ] should restore used quantity on cancel

## Service Tests (CouponServiceTest.java)

### createCoupon
- [ ] should create coupon with all properties
- [ ] should throw exception for duplicate code

### getCoupon
- [ ] should return coupon by id
- [ ] should throw exception when not found

### getCouponByCode
- [ ] should return coupon by code

### issueCouponToMember
- [ ] should issue coupon to member
- [ ] should throw exception when no quantity left

### getMemberCoupons
- [ ] should return member's available coupons
- [ ] should exclude expired coupons

### applyCoupon
- [ ] should calculate discount for valid coupon
- [ ] should throw exception for invalid coupon

### useCoupon
- [ ] should mark member coupon as used
- [ ] should decrement coupon quantity

### validateCoupon
- [ ] should return true for valid coupon
- [ ] should return false when expired
- [ ] should return false when below minimum order
