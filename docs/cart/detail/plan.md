# Cart Module Implementation Plan

## Overview
Shopping cart functionality allowing members to save items before checkout.

## Domain Design

### Entities

#### Cart (Aggregate Root)
```java
@Entity
public class Cart extends BaseEntity {
    private Long memberId;          // Owner
    @OneToMany(mappedBy = "cart")
    private List<CartItem> items;   // Cart items

    // Operations
    addItem(productId, optionId, quantity)
    updateItemQuantity(itemId, quantity)
    removeItem(itemId)
    clear()
    getTotal()
}
```

#### CartItem
```java
@Entity
public class CartItem extends BaseEntity {
    @ManyToOne
    private Cart cart;
    private Long productId;
    private Long productOptionId;
    private int quantity;
    private LocalDateTime addedAt;
}
```

### Business Rules
1. One cart per member (auto-created)
2. Same product+option combination merges quantities
3. Maximum quantity per item: 99
4. Cart persists across sessions (unlike session-based carts)

## Service Layer

### CartService
- getOrCreateCart(memberId): Get existing or create new cart
- addToCart(memberId, request): Add item or merge quantity
- updateQuantity(memberId, cartItemId, quantity): Update item quantity
- removeFromCart(memberId, cartItemId): Remove item
- clearCart(memberId): Remove all items
- getCartSummary(memberId): Get cart with calculated totals

### Cart to Order Conversion
- validateAndConvertToOrder(memberId): Validate stock and create OrderCreateRequest

## DTOs

### Request
- CartItemAddRequest(productId, productOptionId, quantity)
- CartItemUpdateRequest(quantity)

### Response
- CartResponse(id, memberId, items, itemCount, subtotal)
- CartItemResponse(id, productId, productOptionId, productName, optionName, unitPrice, quantity, subtotal)

## Integration Points
- ProductService: Validate product availability and get current prices
- OrderService: Convert cart to order during checkout

## API Endpoints (Future)
- GET /api/cart - Get cart
- POST /api/cart/items - Add item
- PUT /api/cart/items/{itemId} - Update quantity
- DELETE /api/cart/items/{itemId} - Remove item
- DELETE /api/cart - Clear cart
