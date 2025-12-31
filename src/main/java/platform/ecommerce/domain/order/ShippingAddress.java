package platform.ecommerce.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Shipping address value object.
 * Embedded in Order - snapshot at order time.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ShippingAddress {

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "zip_code", nullable = false, length = 10)
    private String zipCode;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "address_detail")
    private String addressDetail;

    public String getFullAddress() {
        if (addressDetail == null || addressDetail.isBlank()) {
            return String.format("(%s) %s", zipCode, address);
        }
        return String.format("(%s) %s %s", zipCode, address, addressDetail);
    }
}
