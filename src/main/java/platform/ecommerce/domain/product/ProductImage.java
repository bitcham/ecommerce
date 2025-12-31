package platform.ecommerce.domain.product;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;

/**
 * Product image entity.
 * Multiple images per product with ordering.
 */
@Entity
@Table(name = "product_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    public ProductImage(Product product, String imageUrl, String altText, int displayOrder) {
        this.product = product;
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.displayOrder = displayOrder;
    }

    /**
     * Update display order.
     */
    public void updateOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * Update image details.
     */
    public void update(String imageUrl, String altText) {
        this.imageUrl = imageUrl;
        this.altText = altText;
    }
}
