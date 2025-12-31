package platform.ecommerce.domain.category;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;
import platform.ecommerce.domain.common.SoftDeletable;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;

import java.time.LocalDateTime;

/**
 * Category aggregate root.
 * Represents a hierarchical product category.
 */
@Entity
@Table(name = "category", indexes = {
        @Index(name = "idx_category_parent", columnList = "parent_id"),
        @Index(name = "idx_category_slug", columnList = "slug")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity implements SoftDeletable {

    public static final int MAX_DEPTH = 3;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false)
    private int depth;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Category(String name, String slug, String description, Long parentId, int depth, int displayOrder) {
        validateName(name);
        validateSlug(slug);
        validateDepth(depth);

        this.name = name;
        this.slug = slug;
        this.description = description;
        this.parentId = parentId;
        this.depth = depth;
        this.displayOrder = displayOrder != 0 ? displayOrder : 0;
        this.active = true;
    }

    /**
     * Update category information.
     */
    public void updateInfo(String name, String slug, String description) {
        validateName(name);
        validateSlug(slug);
        this.name = name;
        this.slug = slug;
        this.description = description;
    }

    /**
     * Update display order.
     */
    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * Move category to new parent.
     */
    public void moveTo(Long newParentId, int newDepth) {
        validateNotSelfReference(newParentId);
        validateDepth(newDepth);
        this.parentId = newParentId;
        this.depth = newDepth;
    }

    /**
     * Activate category.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Deactivate category.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Check if this is a root category.
     */
    public boolean isRoot() {
        return this.parentId == null;
    }

    // ========== SoftDeletable Implementation ==========

    @Override
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    @Override
    public void restore() {
        this.deletedAt = null;
    }

    @Override
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    @Override
    public LocalDateTime getDeletedAt() {
        return this.deletedAt;
    }

    // ========== Validation ==========

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
    }

    private void validateSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Category slug cannot be empty");
        }
        if (!slug.matches("^[a-z0-9-]+$")) {
            throw new IllegalArgumentException("Slug must contain only lowercase letters, numbers, and hyphens");
        }
    }

    private void validateDepth(int depth) {
        if (depth < 0 || depth > MAX_DEPTH) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT,
                    "Category depth must be between 0 and " + MAX_DEPTH);
        }
    }

    private void validateNotSelfReference(Long newParentId) {
        if (newParentId != null && newParentId.equals(this.getId())) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT,
                    "Category cannot be its own parent");
        }
    }
}
