package platform.ecommerce.dto.request.category;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * Request DTO for creating a category.
 */
@Builder
public record CategoryCreateRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Category slug is required")
        @Size(max = 100)
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
        String slug,

        @Size(max = 500)
        String description,

        Long parentId,

        int displayOrder
) {
}
