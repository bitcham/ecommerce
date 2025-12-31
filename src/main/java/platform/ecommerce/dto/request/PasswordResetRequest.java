package platform.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Password reset request DTO.
 */
public record PasswordResetRequest(
        @NotBlank(message = "Token is required")
        String token,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 20, message = "Password must be 8-20 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Password must contain uppercase, lowercase, number, and special character"
        )
        String newPassword
) {
}
