# Cart Module Test Plan

## Domain Tests (CartTest.java)

### Cart Creation
- [ ] should create cart for member
- [ ] should initialize with empty items list

### Add Item
- [ ] should add new item to cart
- [ ] should merge quantity when adding duplicate product+option
- [ ] should throw exception when quantity exceeds maximum

### Update Quantity
- [ ] should update item quantity
- [ ] should throw exception for invalid quantity (0 or negative)
- [ ] should throw exception for quantity exceeding maximum
- [ ] should throw exception when item not found

### Remove Item
- [ ] should remove item from cart
- [ ] should throw exception when item not found

### Clear Cart
- [ ] should remove all items from cart
- [ ] should work on already empty cart

### Calculations
- [ ] should calculate item subtotal correctly
- [ ] should calculate cart total correctly
- [ ] should return zero total for empty cart

## Service Tests (CartServiceTest.java)

### getOrCreateCart
- [ ] should return existing cart for member
- [ ] should create new cart if none exists

### addToCart
- [ ] should add new item to cart
- [ ] should merge quantity for existing item
- [ ] should validate product exists and is available
- [ ] should use current product price

### updateQuantity
- [ ] should update item quantity
- [ ] should throw exception when cart not found
- [ ] should throw exception when item not found
- [ ] should remove item when quantity set to 0

### removeFromCart
- [ ] should remove item from cart
- [ ] should throw exception when item not found

### clearCart
- [ ] should clear all items from cart

### getCartSummary
- [ ] should return cart with product details
- [ ] should calculate totals correctly

## Test Fixtures

### CartFixture
```java
createCart(Long memberId)
createCartWithItems(Long memberId, int itemCount)
createCartItem(Long productId, Long optionId, int quantity)
```
