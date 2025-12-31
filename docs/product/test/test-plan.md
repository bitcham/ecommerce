# Product Module Test Plan

## Domain Unit Tests

### Product Creation
- [x] Should create product with valid data
- [x] Should throw exception for null name
- [x] Should throw exception for negative price
- [x] Should set default status to DRAFT

### Product Status Transitions
- [x] DRAFT → ACTIVE: publish()
- [x] ACTIVE → SOLD_OUT: when all options out of stock
- [x] ACTIVE → DISCONTINUED: discontinue()
- [x] DRAFT → DISCONTINUED: discontinue()
- [x] Should throw exception for invalid transition

### Option Management
- [x] Should add option successfully
- [x] Should remove option successfully
- [x] Should update option stock
- [x] Should calculate total stock from all options
- [x] Should throw exception for negative stock

### Stock Management
- [x] Should decrease stock on purchase
- [x] Should increase stock on cancellation
- [x] Should throw exception for insufficient stock
- [x] Should mark product SOLD_OUT when all options have 0 stock

### Image Management
- [x] Should add image with order
- [x] Should remove image
- [x] Should reorder images
- [x] Should get main image (first in order)

### Soft Delete
- [x] Should mark as deleted with timestamp
- [x] Should be queryable for order history

## Service Unit Tests

### Product CRUD
- [x] Should create product for seller
- [x] Should get product by ID
- [x] Should update product details
- [x] Should soft delete product

### Search/Listing
- [x] Should search products by name
- [x] Should filter by category
- [x] Should filter by status
- [x] Should filter by price range
- [x] Should paginate results

### Inventory Operations
- [x] Should reserve stock on order
- [x] Should release stock on cancellation
- [x] Should handle concurrent stock updates

## Test Fixtures Required
- Product in different statuses
- Product with options
- Product with images
- Mock Seller/Category
