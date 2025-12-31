package platform.ecommerce.dto.response;

import lombok.Builder;

/**
 * Address response DTO.
 */
@Builder
public record AddressResponse(
        Long id,
        String name,
        String recipientName,
        String recipientPhone,
        String zipCode,
        String address,
        String addressDetail,
        String fullAddress,
        boolean isDefault
) {
}
