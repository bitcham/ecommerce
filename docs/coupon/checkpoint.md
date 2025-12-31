# Coupon Module Checkpoint

## Completed Work

### Domain Layer
- **CouponType.java**: Enum (PERCENTAGE, FIXED_AMOUNT)

- **Coupon.java**: Aggregate root for coupons
  - Percentage and fixed amount discounts
  - Minimum order requirement
  - Maximum discount cap for percentage
  - Validity period (validFrom, validTo)
  - Quantity limit with tracking
  - Activation/deactivation

- **MemberCoupon.java**: Issued coupon entity
  - Links coupon to member
  - Usage tracking with order reference
  - Restore capability for cancellations

### Service Layer
- **CouponService.java**: Service interface
  - Create coupons
  - Issue to members
  - Apply discount calculation
  - Use/restore coupons

- **CouponServiceImpl.java**: Service implementation
  - Code uniqueness validation
  - Quantity limit enforcement
  - Discount calculation with caps
  - Member coupon management

### Repository Layer
- **CouponRepository.java**: Coupon queries
- **MemberCouponRepository.java**: Member coupon queries with joins

### DTOs
- **Request**: CouponCreateRequest
- **Response**: CouponResponse, MemberCouponResponse, CouponApplyResponse

### Tests
- **CouponTest.java**: Domain unit tests (22 tests)
- **CouponServiceTest.java**: Service unit tests (14 tests)

## Design Decisions

1. **Dual Discount Types**: Percentage and fixed amount with proper caps
2. **Quantity Management**: Use/restore pattern for order lifecycle
3. **Member Issuance**: One coupon per member to prevent abuse
4. **Code Normalization**: Uppercase codes for consistency

## Test Coverage
- Domain: Creation, validation, discount calculation, usage lifecycle
- Service: All CRUD, issuance, application, validation

## All Core Modules Complete!
