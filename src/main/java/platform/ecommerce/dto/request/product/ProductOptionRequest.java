package platform.ecommerce.dto.request.product;

import jakarta.validation.constraints.*;
import lombok.Builder;
import platform.ecommerce.domain.product.OptionType;

import java.math.BigDecimal;

/**
 * Product option request DTO.
 */
@Builder
public record ProductOptionRequest(

        @NotNull(message = "Option type is required")
        OptionType optionType,

        @NotBlank(message = "Option value is required")
        @Size(max = 100, message = "Option value cannot exceed 100 characters")
        String optionValue,

        @DecimalMin(value = "0", message = "Additional price cannot be negative")
        BigDecimal additionalPrice,

        @Min(value = 0, message = "Stock cannot be negative")
        int stock
) {
}
