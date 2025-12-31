package platform.ecommerce.domain.cart;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cart item entity.
 * Represents a product added to a shopping cart.
 */
@Entity
@Table(name = "cart_item", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cart_id", "product_id", "product_option_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    public static final int MAX_QUANTITY = 99;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_option_id")
    private Long productOptionId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Builder
    public CartItem(Cart cart, Long productId, Long productOptionId, int quantity) {
        validateQuantity(quantity);
        this.cart = cart;
        this.productId = productId;
        this.productOptionId = productOptionId;
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
    }

    /**
     * Update quantity of this item.
     */
    public void updateQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    /**
     * Add to existing quantity.
     */
    public void addQuantity(int additionalQuantity) {
        int newQuantity = this.quantity + additionalQuantity;
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    /**
     * Check if this item matches the given product and option.
     */
    public boolean matches(Long productId, Long productOptionId) {
        if (!this.productId.equals(productId)) {
            return false;
        }
        if (this.productOptionId == null && productOptionId == null) {
            return true;
        }
        return this.productOptionId != null && this.productOptionId.equals(productOptionId);
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity cannot exceed " + MAX_QUANTITY);
        }
    }
}
