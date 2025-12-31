package platform.ecommerce.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Member profile update request DTO.
 */
@Builder
public record MemberUpdateRequest(

        @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
        String name,

        @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "Invalid phone number format")
        String phone,

        @Size(max = 500, message = "Profile image URL cannot exceed 500 characters")
        String profileImage
) {
}
