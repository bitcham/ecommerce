package platform.ecommerce.service.payment;

import java.math.BigDecimal;

/**
 * Payment gateway interface (Strategy pattern).
 * Implementations: MockPaymentGateway, TossPaymentGateway, etc.
 */
public interface PaymentGateway {

    /**
     * Request payment initiation.
     */
    PaymentResult requestPayment(PaymentCommand command);

    /**
     * Confirm payment after user authorization.
     */
    PaymentResult confirmPayment(String transactionId, BigDecimal amount);

    /**
     * Cancel/refund payment.
     */
    PaymentResult cancelPayment(String pgTransactionId, BigDecimal amount);
}
