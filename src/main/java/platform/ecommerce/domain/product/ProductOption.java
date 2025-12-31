package platform.ecommerce.domain.product;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;

import java.math.BigDecimal;

/**
 * Product option entity.
 * Represents variations like color, size with individual stock.
 */
@Entity
@Table(name = "product_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false, length = 20)
    private OptionType optionType;

    @Column(name = "option_value", nullable = false, length = 100)
    private String optionValue;

    @Column(name = "additional_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal additionalPrice;

    @Column(nullable = false)
    private int stock;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    public ProductOption(Product product, OptionType optionType, String optionValue,
                         BigDecimal additionalPrice, int stock, int displayOrder) {
        validateStock(stock);
        this.product = product;
        this.optionType = optionType;
        this.optionValue = optionValue;
        this.additionalPrice = additionalPrice != null ? additionalPrice : BigDecimal.ZERO;
        this.stock = stock;
        this.displayOrder = displayOrder;
    }

    /**
     * Calculate total price including additional price.
     */
    public BigDecimal calculatePrice(BigDecimal basePrice) {
        return basePrice.add(additionalPrice);
    }

    /**
     * Decrease stock for purchase.
     */
    public void decreaseStock(int quantity) {
        if (quantity > this.stock) {
            throw new InvalidStateException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.stock -= quantity;
    }

    /**
     * Increase stock for cancellation or restock.
     */
    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

    /**
     * Update stock directly.
     */
    public void updateStock(int stock) {
        validateStock(stock);
        this.stock = stock;
    }

    /**
     * Check if option is in stock.
     */
    public boolean isInStock() {
        return this.stock > 0;
    }

    /**
     * Update option details.
     */
    public void update(String optionValue, BigDecimal additionalPrice, int displayOrder) {
        this.optionValue = optionValue;
        this.additionalPrice = additionalPrice != null ? additionalPrice : BigDecimal.ZERO;
        this.displayOrder = displayOrder;
    }

    private void validateStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }
}
