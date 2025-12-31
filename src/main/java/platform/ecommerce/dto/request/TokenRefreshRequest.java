package platform.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Token refresh request DTO.
 */
@Builder
public record TokenRefreshRequest(

        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
