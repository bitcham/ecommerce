package platform.ecommerce.domain.cart;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cart aggregate root.
 * Manages shopping cart for a member.
 */
@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Builder
    public Cart(Long memberId) {
        this.memberId = memberId;
    }

    /**
     * Add item to cart. Merges quantity if same product+option exists.
     */
    public CartItem addItem(Long productId, Long productOptionId, int quantity) {
        Optional<CartItem> existingItem = findItem(productId, productOptionId);

        if (existingItem.isPresent()) {
            existingItem.get().addQuantity(quantity);
            return existingItem.get();
        }

        CartItem newItem = CartItem.builder()
                .cart(this)
                .productId(productId)
                .productOptionId(productOptionId)
                .quantity(quantity)
                .build();

        this.items.add(newItem);
        return newItem;
    }

    /**
     * Update item quantity.
     */
    public void updateItemQuantity(Long itemId, int quantity) {
        CartItem item = findItemById(itemId);
        item.updateQuantity(quantity);
    }

    /**
     * Remove item from cart.
     */
    public void removeItem(Long itemId) {
        CartItem item = findItemById(itemId);
        this.items.remove(item);
    }

    /**
     * Clear all items from cart.
     */
    public void clear() {
        this.items.clear();
    }

    /**
     * Get total number of items in cart.
     */
    public int getItemCount() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Get number of unique items in cart.
     */
    public int getUniqueItemCount() {
        return items.size();
    }

    /**
     * Check if cart is empty.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Find item by product and option.
     */
    public Optional<CartItem> findItem(Long productId, Long productOptionId) {
        return items.stream()
                .filter(item -> item.matches(productId, productOptionId))
                .findFirst();
    }

    /**
     * Find item by ID.
     */
    public CartItem findItemById(Long itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CART_ITEM_NOT_FOUND));
    }
}
