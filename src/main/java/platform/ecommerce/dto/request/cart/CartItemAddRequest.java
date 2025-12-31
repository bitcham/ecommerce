package platform.ecommerce.dto.request.cart;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * Request DTO for adding item to cart.
 */
@Builder
public record CartItemAddRequest(

        @NotNull(message = "Product ID is required")
        Long productId,

        Long productOptionId,

        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 99, message = "Quantity cannot exceed 99")
        int quantity
) {
}
