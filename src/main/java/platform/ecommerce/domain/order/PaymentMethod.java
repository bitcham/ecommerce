package platform.ecommerce.domain.order;

/**
 * Payment method enumeration.
 */
public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    BANK_TRANSFER("Bank Transfer"),
    VIRTUAL_ACCOUNT("Virtual Account"),
    MOBILE_PAYMENT("Mobile Payment"),
    CASH_ON_DELIVERY("Cash on Delivery");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
