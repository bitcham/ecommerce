package platform.ecommerce.dto.request.order;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Order item request DTO.
 * Contains product snapshot data at order time.
 */
@Builder
public record OrderItemRequest(

        @NotNull(message = "Product ID is required")
        Long productId,

        Long productOptionId,

        @NotBlank(message = "Product name is required")
        String productName,

        String optionName,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0", message = "Unit price cannot be negative")
        BigDecimal unitPrice,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
}
