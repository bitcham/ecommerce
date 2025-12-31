# Category Module Test Plan

## Domain Tests (CategoryTest.java)

### Category Creation
- [ ] should create root category (no parent)
- [ ] should create child category with parent
- [ ] should set depth based on parent
- [ ] should initialize as active

### Update Info
- [ ] should update name and description
- [ ] should update slug
- [ ] should not allow empty name

### Move Category
- [ ] should move to new parent
- [ ] should update depth when moved
- [ ] should throw exception when moving to self
- [ ] should throw exception when depth exceeds maximum

### Activation
- [ ] should activate category
- [ ] should deactivate category

### Soft Delete
- [ ] should mark as deleted
- [ ] should set deletedAt timestamp

## Service Tests (CategoryServiceTest.java)

### createCategory
- [ ] should create root category
- [ ] should create child category
- [ ] should validate parent exists
- [ ] should validate slug uniqueness

### getCategory
- [ ] should return category by id
- [ ] should throw exception when not found

### getCategoryTree
- [ ] should return hierarchical structure
- [ ] should exclude inactive categories (optional param)

### getRootCategories
- [ ] should return only root level categories

### getChildren
- [ ] should return direct children only
- [ ] should return empty list for leaf category

### updateCategory
- [ ] should update category info
- [ ] should validate slug uniqueness on update

### moveCategory
- [ ] should move category to new parent
- [ ] should update depth of moved category
- [ ] should throw exception for circular reference
- [ ] should throw exception when exceeding max depth

### deleteCategory
- [ ] should soft delete category
- [ ] should throw exception when has children
- [ ] should throw exception when has products

### activate/deactivate
- [ ] should toggle active status
