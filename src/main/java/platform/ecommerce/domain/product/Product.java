package platform.ecommerce.domain.product;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;
import platform.ecommerce.domain.common.SoftDeletable;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Product aggregate root.
 * Manages product options, images, and lifecycle.
 */
@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity implements SoftDeletable {

    private static final int MAX_OPTIONS = 50;
    private static final int MAX_IMAGES = 20;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @Builder
    public Product(String name, String description, BigDecimal basePrice, Long sellerId, Long categoryId) {
        validateRequired(name, "name");
        validatePrice(basePrice);

        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.status = ProductStatus.DRAFT;
    }

    // ========== Status Transitions ==========

    /**
     * Publish product for sale.
     */
    public void publish() {
        if (this.status != ProductStatus.DRAFT) {
            throw new InvalidStateException(ErrorCode.PRODUCT_NOT_AVAILABLE, "Only draft products can be published");
        }
        if (this.options.isEmpty()) {
            throw new InvalidStateException(ErrorCode.PRODUCT_NOT_AVAILABLE, "Product must have at least one option");
        }
        this.status = ProductStatus.ACTIVE;
    }

    /**
     * Discontinue product.
     */
    public void discontinue() {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new InvalidStateException(ErrorCode.PRODUCT_NOT_AVAILABLE, "Product is already discontinued");
        }
        this.status = ProductStatus.DISCONTINUED;
    }

    /**
     * Check and update status based on stock.
     */
    public void updateStatusByStock() {
        if (this.status == ProductStatus.ACTIVE && getTotalStock() == 0) {
            this.status = ProductStatus.SOLD_OUT;
        } else if (this.status == ProductStatus.SOLD_OUT && getTotalStock() > 0) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    // ========== Product Update ==========

    /**
     * Update product details.
     */
    public void update(String name, String description, BigDecimal basePrice, Long categoryId) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.description = description;
        if (basePrice != null) {
            validatePrice(basePrice);
            this.basePrice = basePrice;
        }
        this.categoryId = categoryId;
    }

    // ========== Option Management ==========

    /**
     * Add option to product.
     */
    public ProductOption addOption(OptionType type, String value, BigDecimal additionalPrice, int stock) {
        if (this.options.size() >= MAX_OPTIONS) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Maximum options limit reached");
        }

        int displayOrder = this.options.size();
        ProductOption option = ProductOption.builder()
                .product(this)
                .optionType(type)
                .optionValue(value)
                .additionalPrice(additionalPrice)
                .stock(stock)
                .displayOrder(displayOrder)
                .build();

        this.options.add(option);
        return option;
    }

    /**
     * Remove option from product.
     */
    public void removeOption(Long optionId) {
        ProductOption option = findOptionById(optionId);
        this.options.remove(option);
        updateStatusByStock();
    }

    /**
     * Find option by ID.
     */
    public ProductOption findOptionById(Long optionId) {
        return this.options.stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new InvalidStateException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));
    }

    /**
     * Get total stock across all options.
     */
    public int getTotalStock() {
        return this.options.stream()
                .mapToInt(ProductOption::getStock)
                .sum();
    }

    /**
     * Decrease stock for specific option.
     */
    public void decreaseStock(Long optionId, int quantity) {
        ProductOption option = findOptionById(optionId);
        option.decreaseStock(quantity);
        updateStatusByStock();
    }

    /**
     * Increase stock for specific option.
     */
    public void increaseStock(Long optionId, int quantity) {
        ProductOption option = findOptionById(optionId);
        option.increaseStock(quantity);
        updateStatusByStock();
    }

    // ========== Image Management ==========

    /**
     * Add image to product.
     */
    public ProductImage addImage(String imageUrl, String altText) {
        if (this.images.size() >= MAX_IMAGES) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Maximum images limit reached");
        }

        int displayOrder = this.images.size();
        ProductImage image = ProductImage.builder()
                .product(this)
                .imageUrl(imageUrl)
                .altText(altText)
                .displayOrder(displayOrder)
                .build();

        this.images.add(image);
        return image;
    }

    /**
     * Remove image from product.
     */
    public void removeImage(Long imageId) {
        ProductImage image = findImageById(imageId);
        this.images.remove(image);
        reorderImages();
    }

    /**
     * Find image by ID.
     */
    public ProductImage findImageById(Long imageId) {
        return this.images.stream()
                .filter(i -> i.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new InvalidStateException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
    }

    /**
     * Get main image (first in order).
     */
    public ProductImage getMainImage() {
        return this.images.stream()
                .min(Comparator.comparingInt(ProductImage::getDisplayOrder))
                .orElse(null);
    }

    /**
     * Reorder images after removal.
     */
    private void reorderImages() {
        List<ProductImage> sorted = new ArrayList<>(this.images);
        sorted.sort(Comparator.comparingInt(ProductImage::getDisplayOrder));
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).updateOrder(i);
        }
    }

    // ========== Soft Delete ==========

    @Override
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.status = ProductStatus.DISCONTINUED;
    }

    @Override
    public void restore() {
        if (this.deletedAt == null) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Product is not deleted");
        }
        this.deletedAt = null;
        this.status = ProductStatus.DRAFT;
    }

    // ========== Validation ==========

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }
}
