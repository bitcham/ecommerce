package platform.ecommerce.dto.request.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order creation request DTO.
 */
@Builder
public record OrderCreateRequest(

        @NotNull(message = "Shipping address is required")
        @Valid
        ShippingAddressRequest shippingAddress,

        @NotEmpty(message = "Order must have at least one item")
        @Valid
        List<OrderItemRequest> items,

        BigDecimal shippingFee,
        BigDecimal discountAmount
) {
}
