package platform.ecommerce.dto.response.order;

import lombok.Builder;

/**
 * Shipping address response DTO.
 */
@Builder
public record ShippingAddressResponse(
        String recipientName,
        String recipientPhone,
        String zipCode,
        String address,
        String addressDetail,
        String fullAddress
) {
}
