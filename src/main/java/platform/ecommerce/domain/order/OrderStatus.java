package platform.ecommerce.domain.order;

/**
 * Order status enumeration with state machine logic.
 */
public enum OrderStatus {
    PENDING_PAYMENT("Waiting for payment"),
    PAID("Payment completed"),
    PREPARING("Preparing for shipment"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING_PAYMENT -> next == PAID || next == CANCELLED;
            case PAID -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public boolean canCancel() {
        return this == PENDING_PAYMENT || this == PAID || this == PREPARING;
    }

    public boolean isCompleted() {
        return this == DELIVERED || this == CANCELLED;
    }
}
