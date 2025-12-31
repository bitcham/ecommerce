package platform.ecommerce.service.payment;

import lombok.Builder;

/**
 * Payment result from gateway.
 */
@Builder
public record PaymentResult(
        boolean success,
        String pgTransactionId,
        String failReason
) {
    public static PaymentResult success(String pgTransactionId) {
        return PaymentResult.builder()
                .success(true)
                .pgTransactionId(pgTransactionId)
                .build();
    }

    public static PaymentResult failure(String reason) {
        return PaymentResult.builder()
                .success(false)
                .failReason(reason)
                .build();
    }
}
