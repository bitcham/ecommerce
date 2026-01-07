package platform.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a seller reply.
 */
public record SellerReplyRequest(
        @NotBlank(message = "Reply content is required")
        @Size(max = 5000, message = "Reply content must be at most 5000 characters")
        String content
) {
}
