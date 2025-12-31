# Payment System Test Plan

**Date**: 2025-12-28
**Feature**: Payment System Integration

## Test Scope

### Unit Tests: PaymentService

#### Payment Request
1. **requestPayment_success**
   - Given: Valid order in PENDING_PAYMENT status
   - When: Request payment
   - Then: Returns payment with PENDING status

2. **requestPayment_invalidOrderStatus**
   - Given: Order not in PENDING_PAYMENT status
   - When: Request payment
   - Then: Throws InvalidStateException

3. **requestPayment_orderNotFound**
   - Given: Non-existent order ID
   - When: Request payment
   - Then: Throws EntityNotFoundException

#### Payment Confirmation
4. **confirmPayment_success**
   - Given: Payment in PENDING status
   - When: Confirm payment
   - Then: Payment status → COMPLETED, Order status → PAID

5. **confirmPayment_gatewayFailure**
   - Given: Payment in PENDING status, gateway returns failure
   - When: Confirm payment
   - Then: Payment status → FAILED with reason

6. **confirmPayment_alreadyCompleted**
   - Given: Payment already COMPLETED
   - When: Confirm payment
   - Then: Throws InvalidStateException

#### Payment Cancellation
7. **cancelPayment_success**
   - Given: Payment in COMPLETED status
   - When: Cancel payment
   - Then: Payment status → CANCELLED, refund processed

8. **cancelPayment_notCompleted**
   - Given: Payment in PENDING status
   - When: Cancel payment
   - Then: Throws InvalidStateException

### Unit Tests: MockPaymentGateway

9. **requestPayment_returnsTransactionId**
   - When: Request payment
   - Then: Returns unique transaction ID

10. **confirmPayment_normalAmount_success**
    - Given: Normal payment amount
    - When: Confirm payment
    - Then: Returns success result

11. **confirmPayment_failureAmount_fails**
    - Given: Amount ends with 9999 (failure simulation)
    - When: Confirm payment
    - Then: Returns failure result with reason

12. **cancelPayment_success**
    - Given: Valid transaction ID
    - When: Cancel payment
    - Then: Returns success result

### Integration Tests: PaymentController

1. **POST /payments/request** - 결제 요청 성공
2. **POST /payments/confirm** - 결제 승인 성공/실패
3. **POST /payments/{id}/cancel** - 결제 취소
4. **GET /payments/{id}** - 결제 조회
5. **GET /orders/{orderId}/payments** - 주문별 결제 이력

## Test Data

```java
Order order = Order.builder()
    .memberId(1L)
    .shippingAddress(...)
    .build();
order.addItem(productId, optionId, "Product", "Option", BigDecimal.valueOf(50000), 2);
// Total: 100,000원

// Failure simulation: 금액이 9999로 끝나면 실패
BigDecimal failAmount = BigDecimal.valueOf(109999);
```

## Priority

1. Payment request/confirm (core flow)
2. Payment cancellation (refund)
3. Error handling
4. Payment history
