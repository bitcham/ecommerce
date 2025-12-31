package platform.ecommerce.domain.order;

/**
 * Order item status enumeration.
 */
public enum OrderItemStatus {
    ORDERED("Ordered"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded");

    private final String description;

    OrderItemStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
