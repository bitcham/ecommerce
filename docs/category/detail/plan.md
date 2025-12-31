# Category Module Implementation Plan

## Overview
Hierarchical category system for organizing products. Supports multi-level nesting with parent-child relationships.

## Domain Design

### Entity

#### Category (Aggregate Root)
```java
@Entity
public class Category extends BaseEntity implements SoftDeletable {
    private String name;
    private String slug;              // URL-friendly identifier
    private String description;
    private Long parentId;            // Self-reference for hierarchy
    private int depth;                // Level in tree (0 = root)
    private int displayOrder;         // Sort order among siblings
    private boolean active;

    // Operations
    updateInfo(name, slug, description)
    moveTo(newParentId, newDepth)
    activate() / deactivate()
    delete()
}
```

### Business Rules
1. Category name unique within same parent (siblings)
2. Slug globally unique for URL routing
3. Maximum depth: 3 levels (root → L1 → L2 → L3)
4. Cannot delete category with children or products
5. Cannot set self as parent (circular reference prevention)

## Service Layer

### CategoryService
- createCategory(request): Create new category
- getCategory(id): Get single category
- getCategoryTree(): Get full hierarchy
- getRootCategories(): Get top-level categories
- getChildren(parentId): Get direct children
- updateCategory(id, request): Update category info
- moveCategory(id, newParentId): Change parent
- deleteCategory(id): Soft delete (with validation)
- activateCategory(id) / deactivateCategory(id)

## DTOs

### Request
- CategoryCreateRequest(name, slug, description, parentId, displayOrder)
- CategoryUpdateRequest(name, slug, description, displayOrder)

### Response
- CategoryResponse(id, name, slug, description, parentId, depth, displayOrder, active, childCount)
- CategoryTreeResponse(id, name, slug, children: List<CategoryTreeResponse>)

## API Endpoints (Future)
- GET /api/categories - Get root categories
- GET /api/categories/tree - Get full tree
- GET /api/categories/{id} - Get category
- GET /api/categories/{id}/children - Get children
- POST /api/categories - Create category
- PUT /api/categories/{id} - Update category
- PUT /api/categories/{id}/move - Move to new parent
- DELETE /api/categories/{id} - Delete category
