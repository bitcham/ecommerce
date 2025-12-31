package platform.ecommerce.dto.request.product;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Product update request DTO.
 */
@Builder
public record ProductUpdateRequest(

        @Size(max = 200, message = "Product name cannot exceed 200 characters")
        String name,

        String description,

        @DecimalMin(value = "0", message = "Base price cannot be negative")
        BigDecimal basePrice,

        Long categoryId
) {
}
