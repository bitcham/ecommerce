package platform.ecommerce.domain.product;

/**
 * Product status enumeration.
 */
public enum ProductStatus {
    DRAFT("Draft - Not visible to customers"),
    ACTIVE("Active - Available for sale"),
    SOLD_OUT("Sold out - All options out of stock"),
    DISCONTINUED("Discontinued - No longer available");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canPurchase() {
        return this == ACTIVE;
    }

    public boolean isVisible() {
        return this == ACTIVE || this == SOLD_OUT;
    }
}
