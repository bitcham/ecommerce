package platform.ecommerce.dto.request.cart;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * Request DTO for updating cart item quantity.
 */
@Builder
public record CartItemUpdateRequest(

        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 99, message = "Quantity cannot exceed 99")
        int quantity
) {
}
