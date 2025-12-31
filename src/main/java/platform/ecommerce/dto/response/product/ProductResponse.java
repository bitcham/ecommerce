package platform.ecommerce.dto.response.product;

import lombok.Builder;
import platform.ecommerce.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product summary response DTO.
 */
@Builder
public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        Long sellerId,
        Long categoryId,
        ProductStatus status,
        int totalStock,
        String mainImageUrl,
        LocalDateTime createdAt
) {
}
