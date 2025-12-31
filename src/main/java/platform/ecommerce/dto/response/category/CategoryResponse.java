package platform.ecommerce.dto.response.category;

import lombok.Builder;

/**
 * Category response DTO.
 */
@Builder
public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        Long parentId,
        int depth,
        int displayOrder,
        boolean active,
        int childCount
) {
}
