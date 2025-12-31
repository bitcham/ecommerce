package platform.ecommerce.domain.order;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;

import java.math.BigDecimal;

/**
 * Order item entity.
 * Contains price snapshot at order time.
 */
@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_option_id")
    private Long productOptionId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "option_name", length = 100)
    private String optionName;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderItemStatus status;

    @Builder
    public OrderItem(Order order, Long productId, Long productOptionId, String productName,
                     String optionName, BigDecimal unitPrice, int quantity) {
        validateQuantity(quantity);
        validatePrice(unitPrice);

        this.order = order;
        this.productId = productId;
        this.productOptionId = productOptionId;
        this.productName = productName;
        this.optionName = optionName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.status = OrderItemStatus.ORDERED;
    }

    /**
     * Calculate subtotal for this item.
     */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Cancel this item.
     */
    public void cancel() {
        this.status = OrderItemStatus.CANCELLED;
    }

    /**
     * Mark as shipped.
     */
    public void ship() {
        this.status = OrderItemStatus.SHIPPED;
    }

    /**
     * Mark as delivered.
     */
    public void deliver() {
        this.status = OrderItemStatus.DELIVERED;
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }
}
