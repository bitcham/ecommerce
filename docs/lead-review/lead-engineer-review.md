# Lead Engineer Code Review

**Reviewed by**: Lead Engineer
**Date**: 2025-12-28
**Codebase**: E-Commerce Platform (Spring Boot 3.4.1 + Java 21)

---

## Executive Summary

Overall, this is a **well-architected e-commerce platform** following DDD principles with rich domain models, proper layered architecture, and comprehensive test coverage. However, I've identified **critical security vulnerabilities** that must be addressed immediately.

---

## Critical Issues (Must Fix Immediately)

### 1. BROKEN ACCESS CONTROL - Order Cancellation (CRITICAL)

**File**: `src/main/java/platform/ecommerce/controller/OrderController.java:143-159`

**Issue**: The `cancelOrder` and `cancelOrderItem` endpoints are missing `@PreAuthorize` annotation AND don't verify order ownership. Any authenticated user can cancel ANY order in the system.

```java
// CURRENT (VULNERABLE):
@PostMapping("/{orderId}/cancel")
public ApiResponse<OrderResponse> cancelOrder(...) // No @PreAuthorize!

// REQUIRED FIX:
@PostMapping("/{orderId}/cancel")
@PreAuthorize("isAuthenticated()")
public ApiResponse<OrderResponse> cancelOrder(...) // + ownership check in service
```

**Impact**: HIGH - Malicious users can cancel other users' orders, causing business disruption and potential financial loss.

**Fix**: Add `@PreAuthorize("isAuthenticated()")` and verify `order.getMemberId().equals(currentMemberId)` in service layer.

---

### 2. BROKEN ACCESS CONTROL - Order Information Disclosure (HIGH)

**File**: `src/main/java/platform/ecommerce/controller/OrderController.java:49-64`

**Issue**: `getOrder` and `getOrderByNumber` endpoints don't verify the caller owns the order. Any user can view any order's details including shipping addresses and payment information.

**Impact**: HIGH - Sensitive customer data exposure (addresses, phone numbers, order history).

**Fix**: Add ownership verification in OrderService or use SpEL in @PreAuthorize.

---

### 3. BROKEN ACCESS CONTROL - Payment Information Disclosure (MEDIUM)

**File**: `src/main/java/platform/ecommerce/controller/PaymentController.java:66-84`

**Issue**: `getPayment` and `getPaymentsByOrderId` don't verify ownership. Note: `cancelPayment` correctly validates ownership at line 87.

**Impact**: MEDIUM - Payment transaction details exposed to unauthorized users.

---

## High Priority Issues

### 4. Stock Decrease Before Order Validation (HIGH)

**File**: `src/main/java/platform/ecommerce/service/order/OrderServiceImpl.java:56-74`

**Issue**: Stock is decreased in the loop BEFORE `order.validateForPlacement()` is called. If validation fails, stock is not restored.

```java
// CURRENT (BUG):
for (OrderItemRequest itemRequest : request.items()) {
    order.addItem(...);
    productService.decreaseStock(...);  // Stock decreased
}
order.validateForPlacement();  // Validation happens AFTER stock decrease
```

**Impact**: Inventory inconsistencies if order validation fails.

**Fix**: Move validation before stock operations or wrap in try-catch with rollback.

---

## Medium Priority Issues

### 5. Inconsistent Response Mapping

**Issue**: OrderServiceImpl uses manual `toResponse()` methods (lines 232-278), while MemberService uses MapStruct. This inconsistency makes maintenance harder.

**Recommendation**: Create OrderMapper interface with MapStruct for consistency.

---

### 6. Missing Rate Limiting on Auth Endpoints

**File**: `src/main/java/platform/ecommerce/controller/AuthController.java`

**Issue**: Login and register endpoints lack explicit rate limiting. While Resilience4j is included as dependency, no `@RateLimiter` annotations are visible on auth endpoints.

**Impact**: Vulnerable to brute-force and credential stuffing attacks.

**Recommendation**: Add `@RateLimiter(name = "authRateLimiter")` to login/register endpoints.

---

## Positive Findings

### Excellent Architecture
- Clean DDD with rich domain models (Member, Order, Product as aggregate roots)
- Proper use of Value Objects (ShippingAddress, Email, Money)
- Well-implemented soft delete pattern with SoftDeletable interface

### Strong Error Handling
- Centralized GlobalExceptionHandler with comprehensive coverage
- Well-structured ErrorCode enum with HTTP status mapping
- Proper error classification (Business vs System errors)

### Good Security Foundations
- JWT implementation with refresh token rotation
- BCrypt password encoding
- Method-level security with @PreAuthorize on most endpoints
- Device tracking and IP logging for refresh tokens

### Quality Testing
- Comprehensive unit tests with BDD style (given/when/then)
- Test fixtures (MemberFixture) for reusable test data
- Nested test classes for logical grouping

### Clean Code
- Consistent use of Lombok to reduce boilerplate
- Clear separation of concerns
- Proper transaction management with @Transactional(readOnly = true) by default

---

## Action Items

| Priority | Issue | Files to Modify | Effort |
|----------|-------|-----------------|--------|
| P0 | Fix Order cancel authorization | OrderController, OrderService | 30 min |
| P0 | Fix Order info disclosure | OrderController, OrderService | 30 min |
| P1 | Fix Payment info disclosure | PaymentController, PaymentService | 20 min |
| P1 | Fix stock decrease timing | OrderServiceImpl | 20 min |
| P2 | Add rate limiting to auth | AuthController, RateLimitConfig | 30 min |
| P3 | Add OrderMapper | Create new mapper class | 45 min |

---

## Conclusion

The codebase demonstrates solid engineering practices but has critical authorization gaps that must be addressed before production deployment. The development team has followed good patterns - the issues found are oversights rather than systemic problems.

**Recommendation**: Address P0 issues immediately before any production release.
