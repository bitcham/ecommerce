package platform.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.dto.request.category.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.dto.response.category.*;
import platform.ecommerce.service.category.CategoryService;

import java.util.List;

/**
 * Category REST controller.
 */
@Tag(name = "Category", description = "Category management API")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create category", description = "Create a new category")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        CategoryResponse response = categoryService.createCategory(request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Get category", description = "Get category by ID")
    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId
    ) {
        CategoryResponse response = categoryService.getCategory(categoryId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get category tree", description = "Get category with full subtree")
    @GetMapping("/{categoryId}/tree")
    public ApiResponse<CategoryTreeResponse> getCategoryTree(
            @Parameter(description = "Category ID") @PathVariable Long categoryId
    ) {
        CategoryTreeResponse response = categoryService.getCategoryTree(categoryId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get root categories", description = "Get all root categories")
    @GetMapping("/roots")
    public ApiResponse<List<CategoryResponse>> getRootCategories() {
        List<CategoryResponse> response = categoryService.getRootCategories();
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get full tree", description = "Get complete category tree from all roots")
    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeResponse>> getFullTree() {
        List<CategoryTreeResponse> response = categoryService.getFullTree();
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get children", description = "Get direct children of a category")
    @GetMapping("/{categoryId}/children")
    public ApiResponse<List<CategoryResponse>> getChildren(
            @Parameter(description = "Category ID") @PathVariable Long categoryId
    ) {
        List<CategoryResponse> response = categoryService.getChildren(categoryId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get ancestors", description = "Get all ancestors (breadcrumb path)")
    @GetMapping("/{categoryId}/ancestors")
    public ApiResponse<List<CategoryResponse>> getAncestors(
            @Parameter(description = "Category ID") @PathVariable Long categoryId
    ) {
        List<CategoryResponse> response = categoryService.getAncestors(categoryId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Update category", description = "Update category information")
    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Move category", description = "Move category to new parent")
    @PostMapping("/{categoryId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> moveCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "New parent ID (null for root)") @RequestParam(required = false) Long newParentId
    ) {
        CategoryResponse response = categoryService.moveCategory(categoryId, newParentId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Delete category", description = "Delete category (must have no children or products)")
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId
    ) {
        categoryService.deleteCategory(categoryId);
    }
}
