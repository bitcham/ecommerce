package platform.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Address update request DTO.
 */
@Builder
public record AddressUpdateRequest(

        @NotBlank(message = "Address name is required")
        @Size(max = 50, message = "Address name cannot exceed 50 characters")
        String name,

        @NotBlank(message = "Recipient name is required")
        @Size(max = 100, message = "Recipient name cannot exceed 100 characters")
        String recipientName,

        @NotBlank(message = "Recipient phone is required")
        @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "Invalid phone number format")
        String recipientPhone,

        @NotBlank(message = "Zip code is required")
        @Pattern(regexp = "^\\d{5}$", message = "Invalid zip code format")
        String zipCode,

        @NotBlank(message = "Address is required")
        String address,

        String addressDetail
) {
}
