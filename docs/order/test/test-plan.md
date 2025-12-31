# Order Module Test Plan

## Domain Unit Tests

### Order Creation
- [x] Should create order with valid data
- [x] Should throw exception for empty items
- [x] Should calculate total correctly
- [x] Should set initial status to PENDING_PAYMENT

### Status Transitions
- [x] PENDING_PAYMENT → PAID: markAsPaid()
- [x] PAID → PREPARING: startPreparing()
- [x] PREPARING → SHIPPED: ship()
- [x] SHIPPED → DELIVERED: deliver()
- [x] Should throw exception for invalid transition
- [x] Cannot transition from CANCELLED

### Cancellation
- [x] Should cancel PENDING_PAYMENT order
- [x] Should cancel PAID order
- [x] Should throw exception for SHIPPED order
- [x] Should throw exception for DELIVERED order

### Order Items
- [x] Should add item with quantity
- [x] Should calculate item subtotal
- [x] Should snapshot product price
- [x] Should update item status on order status change

### Payment Info
- [x] Should record payment method
- [x] Should record payment timestamp
- [x] Should track payment transaction ID

## Service Unit Tests

### Order CRUD
- [x] Should create order from cart
- [x] Should get order by ID
- [x] Should get orders by member
- [x] Should get orders by seller

### Status Operations
- [x] Should process payment
- [x] Should update shipping status
- [x] Should handle delivery confirmation

### Cancellation
- [x] Should cancel order and restore stock
- [x] Should handle partial cancellation

## Test Fixtures Required
- Order in different statuses
- Order with multiple items
- Mock Product/Member references
