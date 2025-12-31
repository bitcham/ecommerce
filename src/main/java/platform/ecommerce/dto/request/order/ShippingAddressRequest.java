package platform.ecommerce.dto.request.order;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * Shipping address request DTO.
 */
@Builder
public record ShippingAddressRequest(

        @NotBlank(message = "Recipient name is required")
        @Size(max = 100)
        String recipientName,

        @NotBlank(message = "Recipient phone is required")
        @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "Invalid phone format")
        String recipientPhone,

        @NotBlank(message = "Zip code is required")
        @Pattern(regexp = "^\\d{5}$", message = "Invalid zip code format")
        String zipCode,

        @NotBlank(message = "Address is required")
        String address,

        String addressDetail
) {
}
