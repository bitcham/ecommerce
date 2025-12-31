# Cart Module Checkpoint

## Completed Work

### Domain Layer
- **Cart.java**: Aggregate root for shopping cart
  - One cart per member (unique constraint)
  - Items management with merge logic for duplicates
  - Clear all items functionality
  - Item count calculations (total and unique)

- **CartItem.java**: Cart item entity
  - Product and option reference
  - Quantity with validation (1-99)
  - Matching logic for product+option combination
  - AddedAt timestamp

### Service Layer
- **CartService.java**: Service interface
  - getOrCreateCart: Auto-create cart on first access
  - addToCart: Add item with merge support
  - updateQuantity: Update item quantity
  - removeFromCart: Remove specific item
  - clearCart: Clear all items
  - getCartSummary: Get cart with product details

- **CartServiceImpl.java**: Service implementation
  - Product validation via ProductService
  - Dynamic price calculation from ProductDetailResponse
  - Graceful handling of unavailable products
  - Transaction management

### Repository Layer
- **CartRepository.java**: JPA repository with fetch join for items

### DTOs
- **Request**: CartItemAddRequest, CartItemUpdateRequest
- **Response**: CartResponse, CartItemResponse

### Tests
- **CartTest.java**: Domain unit tests (17 tests)
  - Creation, add item, merge, update, remove, clear
  - Quantity validation, matching logic
- **CartServiceTest.java**: Service unit tests (14 tests)
  - All operations with mocked dependencies

## Design Decisions

1. **One Cart Per Member**: Enforced by unique constraint on memberId
2. **Quantity Merge**: Adding same product+option increases quantity instead of creating duplicate
3. **Max Quantity**: 99 per item to prevent abuse
4. **Dynamic Pricing**: Cart retrieves current prices from ProductService on each summary request
5. **Graceful Degradation**: Unavailable products marked as such rather than causing errors

## Test Coverage
- Domain: All cart operations, item matching, validation
- Service: CRUD operations, product integration, error handling

## Next Steps
- Consider adding: Cart expiration, inventory reservation
- API Controller implementation
