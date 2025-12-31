package platform.ecommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Email verification request DTO.
 */
@Builder
public record EmailVerificationRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {
}
