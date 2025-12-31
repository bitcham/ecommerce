package platform.ecommerce.dto.response.product;

import lombok.Builder;
import platform.ecommerce.domain.product.OptionType;

import java.math.BigDecimal;

/**
 * Product option response DTO.
 */
@Builder
public record ProductOptionResponse(
        Long id,
        OptionType optionType,
        String optionValue,
        BigDecimal additionalPrice,
        int stock,
        int displayOrder,
        boolean inStock
) {
}
