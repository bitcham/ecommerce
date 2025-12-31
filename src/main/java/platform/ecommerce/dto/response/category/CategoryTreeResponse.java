package platform.ecommerce.dto.response.category;

import lombok.Builder;

import java.util.List;

/**
 * Category tree response DTO for hierarchical display.
 */
@Builder
public record CategoryTreeResponse(
        Long id,
        String name,
        String slug,
        int displayOrder,
        List<CategoryTreeResponse> children
) {
}
