package platform.ecommerce.dto.request.review;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

/**
 * Request DTO for updating a review.
 */
@Builder
public record ReviewUpdateRequest(

        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        int rating,

        @Size(max = 200)
        String title,

        @Size(max = 5000)
        String content,

        @Size(max = 5, message = "Maximum 5 images allowed")
        List<String> images
) {
}
