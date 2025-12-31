package platform.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Password change request DTO.
 */
@Builder
public record PasswordChangeRequest(

        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 20, message = "Password must be 8-20 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Password must contain uppercase, lowercase, number, and special character"
        )
        String newPassword,

        @NotBlank(message = "New password confirmation is required")
        String newPasswordConfirm
) {
    public boolean isNewPasswordMatched() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }
}
