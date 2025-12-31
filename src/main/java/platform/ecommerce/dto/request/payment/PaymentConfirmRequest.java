package platform.ecommerce.dto.request.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Payment confirmation request DTO.
 */
@Builder
public record PaymentConfirmRequest(
        @NotBlank(message = "Transaction ID is required")
        String transactionId,

        @NotNull(message = "Amount is required")
        BigDecimal amount
) {
}
