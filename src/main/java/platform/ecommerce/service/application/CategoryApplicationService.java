package platform.ecommerce.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import platform.ecommerce.dto.request.category.CategoryCreateRequest;
import platform.ecommerce.dto.request.category.CategoryUpdateRequest;
import platform.ecommerce.dto.response.category.CategoryResponse;
import platform.ecommerce.dto.response.category.CategoryTreeResponse;
import platform.ecommerce.service.category.CategoryService;

import java.util.List;

/**
 * Category application service.
 * Currently delegates to CategoryService.
 */
@Service
@RequiredArgsConstructor
public class CategoryApplicationService {

    private final CategoryService categoryService;

    public CategoryResponse createCategory(CategoryCreateRequest request) {
        return categoryService.createCategory(request);
    }

    public CategoryResponse getCategory(Long categoryId) {
        return categoryService.getCategory(categoryId);
    }

    public CategoryTreeResponse getCategoryTree(Long categoryId) {
        return categoryService.getCategoryTree(categoryId);
    }

    public List<CategoryResponse> getRootCategories() {
        return categoryService.getRootCategories();
    }

    public List<CategoryTreeResponse> getFullTree() {
        return categoryService.getFullTree();
    }

    public List<CategoryResponse> getChildren(Long categoryId) {
        return categoryService.getChildren(categoryId);
    }

    public List<CategoryResponse> getAncestors(Long categoryId) {
        return categoryService.getAncestors(categoryId);
    }

    public CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request) {
        return categoryService.updateCategory(categoryId, request);
    }

    public CategoryResponse moveCategory(Long categoryId, Long newParentId) {
        return categoryService.moveCategory(categoryId, newParentId);
    }

    public void deleteCategory(Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }
}
