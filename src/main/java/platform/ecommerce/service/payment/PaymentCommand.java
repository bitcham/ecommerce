package platform.ecommerce.service.payment;

import lombok.Builder;
import platform.ecommerce.domain.order.PaymentMethod;

import java.math.BigDecimal;

/**
 * Payment command for gateway request.
 */
@Builder
public record PaymentCommand(
        String transactionId,
        String orderNumber,
        BigDecimal amount,
        PaymentMethod method,
        String customerName,
        String customerEmail
) {
}
