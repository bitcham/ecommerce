package platform.ecommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Member registration request DTO.
 */
@Builder
public record MemberCreateRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 20, message = "Password must be 8-20 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Password must contain uppercase, lowercase, number, and special character"
        )
        String password,

        @NotBlank(message = "Password confirmation is required")
        String passwordConfirm,

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
        String name,

        @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "Invalid phone number format")
        String phone
) {
    public boolean isPasswordMatched() {
        return password != null && password.equals(passwordConfirm);
    }
}
