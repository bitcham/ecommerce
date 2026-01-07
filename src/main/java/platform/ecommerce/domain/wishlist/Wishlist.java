package platform.ecommerce.domain.wishlist;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;

/**
 * Wishlist entity representing a member's wishlist item.
 */
@Entity
@Table(
        name = "wishlist",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wishlist_member_product",
                columnNames = {"member_id", "product_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist extends BaseEntity {

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Builder
    public Wishlist(Long memberId, Long productId) {
        this.memberId = memberId;
        this.productId = productId;
    }

    /**
     * Factory method to create a wishlist item.
     */
    public static Wishlist of(Long memberId, Long productId) {
        return Wishlist.builder()
                .memberId(memberId)
                .productId(productId)
                .build();
    }
}
