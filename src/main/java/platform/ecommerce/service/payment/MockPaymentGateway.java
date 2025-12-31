package platform.ecommerce.service.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock payment gateway for development and testing.
 * Simulates failure when amount ends with 9999.
 */
@Slf4j
@Component
public class MockPaymentGateway implements PaymentGateway {

    private static final BigDecimal FAILURE_AMOUNT_SUFFIX = new BigDecimal("9999");

    @Override
    public PaymentResult requestPayment(PaymentCommand command) {
        log.info("Mock: Requesting payment for transaction={}, amount={}",
                command.transactionId(), command.amount());

        // Generate mock PG transaction ID
        String pgTransactionId = "PG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return PaymentResult.success(pgTransactionId);
    }

    @Override
    public PaymentResult confirmPayment(String transactionId, BigDecimal amount) {
        log.info("Mock: Confirming payment for transaction={}, amount={}", transactionId, amount);

        // Simulate failure for amounts ending with 9999
        if (shouldSimulateFailure(amount)) {
            log.warn("Mock: Simulating payment failure for amount={}", amount);
            return PaymentResult.failure("Simulated payment failure: Card declined");
        }

        String pgTransactionId = "PG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResult.success(pgTransactionId);
    }

    @Override
    public PaymentResult cancelPayment(String pgTransactionId, BigDecimal amount) {
        log.info("Mock: Cancelling payment pgTxId={}, amount={}", pgTransactionId, amount);

        // Always succeed for cancellation
        return PaymentResult.success(pgTransactionId);
    }

    private boolean shouldSimulateFailure(BigDecimal amount) {
        // Check if amount ends with 9999
        BigDecimal remainder = amount.remainder(new BigDecimal("10000"));
        return remainder.compareTo(FAILURE_AMOUNT_SUFFIX) == 0;
    }
}
