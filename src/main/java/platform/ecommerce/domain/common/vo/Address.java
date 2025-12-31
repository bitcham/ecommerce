package platform.ecommerce.domain.common.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object representing a postal address.
 * Immutable.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "address_detail", length = 255)
    private String addressDetail;

    @Builder
    public Address(String zipCode, String address, String addressDetail) {
        validateZipCode(zipCode);
        validateAddress(address);
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
    }

    private void validateZipCode(String zipCode) {
        if (zipCode == null || zipCode.isBlank()) {
            throw new IllegalArgumentException("Zip code cannot be null or empty");
        }
        if (!zipCode.matches("^\\d{5}$")) {
            throw new IllegalArgumentException("Invalid zip code format. Expected 5 digits.");
        }
    }

    private void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        if (address.length() > 255) {
            throw new IllegalArgumentException("Address cannot exceed 255 characters");
        }
    }

    public String getFullAddress() {
        if (addressDetail == null || addressDetail.isBlank()) {
            return address;
        }
        return address + " " + addressDetail;
    }

    @Override
    public String toString() {
        return String.format("(%s) %s", zipCode, getFullAddress());
    }
}
