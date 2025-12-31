package platform.ecommerce.dto.response.product;

import lombok.Builder;
import platform.ecommerce.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product detail response DTO.
 */
@Builder
public record ProductDetailResponse(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        Long sellerId,
        Long categoryId,
        ProductStatus status,
        int totalStock,
        List<ProductOptionResponse> options,
        List<ProductImageResponse> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
