# Order Domain Implementation Plan

## Goal
Implement Order aggregate root with order items, status transitions, and payment tracking for e-commerce order management.

## Approach

### Domain Model
```
Order (Aggregate Root)
├── OrderItem[]          (Products ordered)
├── ShippingAddress      (Value Object)
├── PaymentInfo          (Value Object)
└── OrderStatus          (State machine)

OrderItem
├── Product reference
├── Option reference
├── Quantity
├── Price snapshot
└── ItemStatus
```

### Core Features
1. **Order Creation**: Create from cart items with price snapshot
2. **Status Management**: State machine (PENDING → PAID → SHIPPED → DELIVERED)
3. **Cancellation**: Full or partial with stock restoration
4. **Payment Tracking**: Payment status and method
5. **Shipping**: Address and tracking info

### Order Status Flow
```
PENDING_PAYMENT → PAID → PREPARING → SHIPPED → DELIVERED
       ↓           ↓        ↓
   CANCELLED   CANCELLED  CANCELLED (partial refund)
```

### Business Rules
- Price snapshot at order time (immutable)
- Stock decrease on order creation
- Stock restore on cancellation
- Cannot cancel after SHIPPED
- Order total = sum(item price * quantity) + shipping - discount

## Trade-offs

| Decision | Trade-off |
|----------|-----------|
| Price snapshot | Data duplication vs. price consistency |
| Status as enum | Simple but limited flexibility |
| Soft delete | Audit trail vs. storage |
| Embedded shipping address | Denormalization vs. address changes |

## Dependencies
- Product module (stock operations)
- Member module (buyer info)
- Payment module (external integration)

## Error Scenarios
- Order not found → ORDER_NOT_FOUND
- Invalid status transition → ORDER_STATUS_INVALID
- Cannot cancel → ORDER_CANNOT_CANCEL
- Insufficient stock → INSUFFICIENT_STOCK
