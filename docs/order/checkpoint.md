# Order Module Checkpoint

## Completed Work

### Domain Layer
- **Order.java**: Aggregate root managing order lifecycle
  - State machine pattern for status transitions (PENDING_PAYMENT → PAID → PREPARING → SHIPPED → DELIVERED)
  - Items management with price snapshots
  - Shipping address as embedded value object
  - Automatic order number generation
  - Total calculation (subtotal + shipping - discount)

- **OrderItem.java**: Order item entity
  - Price and product snapshot at order time
  - Item-level status tracking

- **OrderStatus.java**: Status enum with transition validation
  - `canTransitionTo()` for valid state changes
  - `canCancel()` for cancellation eligibility

- **OrderItemStatus.java**: Item status tracking (ORDERED, SHIPPED, DELIVERED, CANCELLED)

- **PaymentMethod.java**: Payment method enumeration

- **ShippingAddress.java**: Embedded value object for delivery address

### Service Layer
- **OrderService.java**: Service interface
  - Order creation with stock management
  - Status transition operations (payment, preparing, ship, deliver)
  - Cancellation with stock restoration

- **OrderServiceImpl.java**: Service implementation
  - Integration with ProductService for stock operations
  - Transaction management
  - DTO mapping

### Repository Layer
- **OrderRepository.java**: JPA repository with custom queries
- **OrderQueryRepository.java**: QueryDSL interface for complex queries
- **OrderQueryRepositoryImpl.java**: QueryDSL implementation with dynamic search

### DTOs
- **Request**: OrderCreateRequest, OrderItemRequest, ShippingAddressRequest, PaymentRequest, OrderSearchCondition
- **Response**: OrderResponse, OrderItemResponse, ShippingAddressResponse

### Tests
- **OrderTest.java**: Domain unit tests (15 tests)
- **OrderServiceTest.java**: Service unit tests (14 tests)

## Design Decisions

1. **Price Snapshot Pattern**: Order items store product name and price at order time to preserve historical data
2. **State Machine**: OrderStatus enum enforces valid transitions
3. **Stock Coordination**: Service layer coordinates with ProductService for stock operations during order creation and cancellation
4. **Aggregate Root**: Order manages OrderItem lifecycle through cascade operations

## Test Coverage
- Domain tests: Order creation, status transitions, item management, cancellation
- Service tests: All CRUD operations, payment processing, stock management integration

## Next Module: Cart
