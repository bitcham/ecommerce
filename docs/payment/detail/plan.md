# Payment System Integration Plan

**Date**: 2025-12-28
**Feature**: Payment System Integration

## Goal

결제 시스템을 추상화하여 다양한 PG사 연동이 가능하도록 하고, 결제 이력을 관리하는 기능을 구현한다.

## Current State Analysis

### Existing Implementation
- `Order.markAsPaid()`: 결제 완료 처리 (상태 변경만)
- `OrderService.processPayment()`: transactionId 기록
- `PaymentRequest`: paymentMethod + transactionId

### Limitations
1. 실제 PG 연동 없음
2. 결제 이력 추적 불가
3. 환불 처리 없음
4. 결제 상태 관리 없음

## Approach

### Phase 1: Payment Abstraction (MVP)

#### New Components

1. **Payment Entity**
   - 결제 이력 저장
   - 주문과 1:N 관계 (부분 결제, 재결제 고려)

2. **PaymentGateway Interface (Strategy Pattern)**
   - `requestPayment()`: 결제 요청
   - `confirmPayment()`: 결제 승인
   - `cancelPayment()`: 결제 취소/환불

3. **MockPaymentGateway**
   - 개발/테스트용 Mock 구현
   - 특정 금액에서 실패 시뮬레이션

4. **PaymentService**
   - 결제 처리 비즈니스 로직
   - Gateway 선택 및 호출

### Phase 2: Future (Not in scope)
- 실제 PG 연동 (Toss, KG이니시스)
- Webhook 처리
- 부분 환불

## Design Decisions

| 항목 | 선택 | 이유 |
|------|------|------|
| Gateway 패턴 | Strategy | PG사 변경/추가 용이 |
| Payment Entity | 별도 테이블 | 결제 이력 추적 필요 |
| Mock 구현 | 금액 기반 | 다양한 시나리오 테스트 |

## Implementation Details

### Payment Entity
```java
@Entity
public class Payment {
    private Long id;
    private Long orderId;
    private PaymentMethod method;
    private PaymentStatus status;  // PENDING, COMPLETED, FAILED, CANCELLED
    private BigDecimal amount;
    private String transactionId;
    private String pgTransactionId;
    private String failReason;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
}
```

### PaymentGateway Interface
```java
public interface PaymentGateway {
    PaymentResult requestPayment(PaymentCommand command);
    PaymentResult confirmPayment(String transactionId);
    PaymentResult cancelPayment(String transactionId, BigDecimal amount);
}
```

### PaymentStatus Enum
- PENDING: 결제 대기
- COMPLETED: 결제 완료
- FAILED: 결제 실패
- CANCELLED: 결제 취소

## API Endpoints

1. `POST /api/v1/payments/request` - 결제 요청
2. `POST /api/v1/payments/confirm` - 결제 승인
3. `POST /api/v1/payments/{paymentId}/cancel` - 결제 취소
4. `GET /api/v1/payments/{paymentId}` - 결제 조회
5. `GET /api/v1/orders/{orderId}/payments` - 주문별 결제 이력

## Dependencies

- Order 엔티티와 연관
- OrderService와 연동 (결제 완료 시 상태 변경)

## Risks

- 결제 중 서버 장애 → 트랜잭션 복구 로직 필요
- PG 응답 지연 → 타임아웃 처리

## Checklist

- [ ] PaymentStatus enum
- [ ] Payment entity
- [ ] PaymentGateway interface
- [ ] MockPaymentGateway implementation
- [ ] PaymentService interface & implementation
- [ ] PaymentController
- [ ] DB migration
- [ ] Tests
