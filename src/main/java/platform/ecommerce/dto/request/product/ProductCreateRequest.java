package platform.ecommerce.dto.request.product;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Product creation request DTO.
 */
@Builder
public record ProductCreateRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name cannot exceed 200 characters")
        String name,

        String description,

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0", message = "Base price cannot be negative")
        BigDecimal basePrice,

        Long categoryId
) {
}
