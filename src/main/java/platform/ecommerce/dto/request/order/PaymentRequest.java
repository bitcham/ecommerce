package platform.ecommerce.dto.request.order;

import jakarta.validation.constraints.*;
import lombok.Builder;
import platform.ecommerce.domain.order.PaymentMethod;

/**
 * Payment request DTO.
 */
@Builder
public record PaymentRequest(

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @NotBlank(message = "Transaction ID is required")
        String transactionId
) {
}
