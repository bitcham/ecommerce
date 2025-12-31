package platform.ecommerce.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.config.CacheConfig;
import platform.ecommerce.domain.category.Category;
import platform.ecommerce.dto.request.category.CategoryCreateRequest;
import platform.ecommerce.dto.request.category.CategoryUpdateRequest;
import platform.ecommerce.dto.response.category.CategoryResponse;
import platform.ecommerce.dto.response.category.CategoryTreeResponse;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;
import platform.ecommerce.repository.category.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Category service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CATEGORY_TREE_CACHE, allEntries = true)
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        log.info("Creating category: {}", request.name());

        validateSlugUnique(request.slug(), null);

        int depth = 0;
        if (request.parentId() != null) {
            Category parent = findCategoryById(request.parentId());
            depth = parent.getDepth() + 1;
            validateDepth(depth);
        }

        Category category = Category.builder()
                .name(request.name())
                .slug(request.slug())
                .description(request.description())
                .parentId(request.parentId())
                .depth(depth)
                .displayOrder(request.displayOrder())
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created: id={}", savedCategory.getId());

        return toResponse(savedCategory);
    }

    @Override
    @Cacheable(value = CacheConfig.CATEGORY_CACHE, key = "#categoryId")
    public CategoryResponse getCategory(Long categoryId) {
        Category category = findCategoryById(categoryId);
        return toResponse(category);
    }

    @Override
    @Cacheable(value = CacheConfig.CATEGORY_TREE_CACHE, key = "'root'")
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAllActive();

        // Group categories by parent ID
        Map<Long, List<Category>> categoryByParent = allCategories.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getParentId() != null ? c.getParentId() : 0L
                ));

        // Build tree starting from root categories (parentId = null, grouped under 0L)
        return buildTree(categoryByParent, 0L);
    }

    @Override
    public CategoryTreeResponse getCategoryTree(Long categoryId) {
        Category category = findCategoryById(categoryId);
        List<Category> allCategories = categoryRepository.findAllActive();

        // Group categories by parent ID
        Map<Long, List<Category>> categoryByParent = allCategories.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getParentId() != null ? c.getParentId() : 0L
                ));

        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .displayOrder(category.getDisplayOrder())
                .children(buildTree(categoryByParent, category.getId()))
                .build();
    }

    @Override
    public List<CategoryTreeResponse> getFullTree() {
        return getCategoryTree();
    }

    @Override
    public List<CategoryResponse> getAncestors(Long categoryId) {
        List<CategoryResponse> ancestors = new ArrayList<>();
        Category current = findCategoryById(categoryId);

        while (current.getParentId() != null) {
            current = findCategoryById(current.getParentId());
            ancestors.add(0, toResponse(current)); // Add at beginning for correct order
        }

        return ancestors;
    }

    @Override
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findRootCategories().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> getChildren(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CATEGORY_CACHE, key = "#categoryId"),
            @CacheEvict(value = CacheConfig.CATEGORY_TREE_CACHE, allEntries = true)
    })
    public CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request) {
        log.info("Updating category: id={}", categoryId);

        Category category = findCategoryById(categoryId);
        validateSlugUnique(request.slug(), categoryId);

        category.updateInfo(request.name(), request.slug(), request.description());
        category.updateDisplayOrder(request.displayOrder());

        log.info("Category updated: id={}", categoryId);
        return toResponse(category);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CATEGORY_CACHE, key = "#categoryId"),
            @CacheEvict(value = CacheConfig.CATEGORY_TREE_CACHE, allEntries = true)
    })
    public CategoryResponse moveCategory(Long categoryId, Long newParentId) {
        log.info("Moving category: id={} to parent={}", categoryId, newParentId);

        Category category = findCategoryById(categoryId);

        int newDepth = 0;
        if (newParentId != null) {
            Category newParent = findCategoryById(newParentId);
            validateNotDescendant(category, newParent);
            newDepth = newParent.getDepth() + 1;
        }

        category.moveTo(newParentId, newDepth);

        log.info("Category moved: id={}", categoryId);
        return toResponse(category);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CATEGORY_CACHE, key = "#categoryId"),
            @CacheEvict(value = CacheConfig.CATEGORY_TREE_CACHE, allEntries = true)
    })
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category: id={}", categoryId);

        Category category = findCategoryById(categoryId);
        validateCanDelete(category);

        category.delete();

        log.info("Category deleted: id={}", categoryId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CATEGORY_CACHE, key = "#categoryId"),
            @CacheEvict(value = CacheConfig.CATEGORY_TREE_CACHE, allEntries = true)
    })
    public CategoryResponse activateCategory(Long categoryId) {
        log.info("Activating category: id={}", categoryId);

        Category category = findCategoryById(categoryId);
        category.activate();

        return toResponse(category);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CATEGORY_CACHE, key = "#categoryId"),
            @CacheEvict(value = CacheConfig.CATEGORY_TREE_CACHE, allEntries = true)
    })
    public CategoryResponse deactivateCategory(Long categoryId) {
        log.info("Deactivating category: id={}", categoryId);

        Category category = findCategoryById(categoryId);
        category.deactivate();

        return toResponse(category);
    }

    // ========== Private Helper Methods ==========

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findByIdNotDeleted(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private void validateSlugUnique(String slug, Long excludeId) {
        categoryRepository.findBySlug(slug)
                .filter(existing -> !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    throw new InvalidStateException(ErrorCode.CONFLICT, "Slug already exists: " + slug);
                });
    }

    private void validateDepth(int depth) {
        if (depth > Category.MAX_DEPTH) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT,
                    "Maximum category depth exceeded. Max depth: " + Category.MAX_DEPTH);
        }
    }

    private void validateNotDescendant(Category category, Category newParent) {
        // Check if newParent is a descendant of category (would create circular reference)
        Long parentId = newParent.getParentId();
        while (parentId != null) {
            if (parentId.equals(category.getId())) {
                throw new InvalidStateException(ErrorCode.INVALID_INPUT,
                        "Cannot move category to its own descendant");
            }
            Category parent = categoryRepository.findById(parentId).orElse(null);
            parentId = parent != null ? parent.getParentId() : null;
        }
    }

    private void validateCanDelete(Category category) {
        if (categoryRepository.hasChildren(category.getId())) {
            throw new InvalidStateException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }
        // Note: Check for products would be added when Product-Category relationship is implemented
    }

    private List<CategoryTreeResponse> buildTree(Map<Long, List<Category>> categoryByParent, Long parentId) {
        List<Category> children = categoryByParent.getOrDefault(parentId, new ArrayList<>());

        return children.stream()
                .map(category -> CategoryTreeResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .slug(category.getSlug())
                        .displayOrder(category.getDisplayOrder())
                        .children(buildTree(categoryByParent, category.getId()))
                        .build())
                .toList();
    }

    private CategoryResponse toResponse(Category category) {
        int childCount = categoryRepository.countChildren(category.getId());
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .depth(category.getDepth())
                .displayOrder(category.getDisplayOrder())
                .active(category.isActive())
                .childCount(childCount)
                .build();
    }
}
