package platform.ecommerce.domain.member;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;

/**
 * Member delivery address entity.
 * Owned by Member aggregate root.
 */
@Entity
@Table(name = "member_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAddress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "zip_code", nullable = false, length = 10)
    private String zipCode;

    @Column(nullable = false)
    private String address;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Builder
    public MemberAddress(Member member, String name, String recipientName, String recipientPhone,
                         String zipCode, String address, String addressDetail, boolean isDefault) {
        this.member = member;
        this.name = name;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.isDefault = isDefault;
    }

    /**
     * Update address information.
     */
    public void update(String name, String recipientName, String recipientPhone,
                       String zipCode, String address, String addressDetail) {
        this.name = name;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
    }

    /**
     * Set as default address.
     * Package-private to be called only by Member aggregate.
     */
    void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * Get full address string.
     */
    public String getFullAddress() {
        if (addressDetail == null || addressDetail.isBlank()) {
            return String.format("(%s) %s", zipCode, address);
        }
        return String.format("(%s) %s %s", zipCode, address, addressDetail);
    }
}
