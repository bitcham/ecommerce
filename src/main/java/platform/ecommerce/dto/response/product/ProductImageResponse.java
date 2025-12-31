package platform.ecommerce.dto.response.product;

import lombok.Builder;

/**
 * Product image response DTO.
 */
@Builder
public record ProductImageResponse(
        Long id,
        String imageUrl,
        String altText,
        int displayOrder
) {
}
