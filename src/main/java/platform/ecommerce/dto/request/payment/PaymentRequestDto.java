package platform.ecommerce.dto.request.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import platform.ecommerce.domain.order.PaymentMethod;

/**
 * Payment request DTO for requesting payment.
 */
@Builder
public record PaymentRequestDto(
        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotNull(message = "Payment method is required")
        PaymentMethod method
) {
}
