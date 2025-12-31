package platform.ecommerce.domain.payment;

/**
 * Payment status enumeration.
 */
public enum PaymentStatus {
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canConfirm() {
        return this == PENDING;
    }

    public boolean canCancel() {
        return this == COMPLETED;
    }
}
