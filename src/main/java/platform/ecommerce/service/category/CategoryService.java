package platform.ecommerce.service.category;

import platform.ecommerce.dto.request.category.CategoryCreateRequest;
import platform.ecommerce.dto.request.category.CategoryUpdateRequest;
import platform.ecommerce.dto.response.category.CategoryResponse;
import platform.ecommerce.dto.response.category.CategoryTreeResponse;

import java.util.List;

/**
 * Service interface for Category operations.
 */
public interface CategoryService {

    /**
     * Creates a new category.
     */
    CategoryResponse createCategory(CategoryCreateRequest request);

    /**
     * Gets a category by ID.
     */
    CategoryResponse getCategory(Long categoryId);

    /**
     * Gets the full category tree.
     */
    List<CategoryTreeResponse> getCategoryTree();

    /**
     * Gets category tree starting from a specific category.
     */
    CategoryTreeResponse getCategoryTree(Long categoryId);

    /**
     * Gets the complete category tree from all roots.
     */
    List<CategoryTreeResponse> getFullTree();

    /**
     * Gets all ancestors (breadcrumb path) for a category.
     */
    List<CategoryResponse> getAncestors(Long categoryId);

    /**
     * Gets root level categories.
     */
    List<CategoryResponse> getRootCategories();

    /**
     * Gets direct children of a category.
     */
    List<CategoryResponse> getChildren(Long parentId);

    /**
     * Updates a category.
     */
    CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request);

    /**
     * Moves a category to a new parent.
     */
    CategoryResponse moveCategory(Long categoryId, Long newParentId);

    /**
     * Deletes a category (soft delete).
     */
    void deleteCategory(Long categoryId);

    /**
     * Activates a category.
     */
    CategoryResponse activateCategory(Long categoryId);

    /**
     * Deactivates a category.
     */
    CategoryResponse deactivateCategory(Long categoryId);
}
