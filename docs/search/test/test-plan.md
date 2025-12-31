# Product Search Enhancement Test Plan

**Date**: 2025-12-28
**Feature**: Product Search Enhancement

## Test Scope

### Unit Tests: ProductQueryRepositoryImpl

#### Keyword Search Tests
1. **keywordSearchesNameAndDescription**
   - Given: Products with matching name or description
   - When: Search with keyword
   - Then: Returns products matching in either field

2. **keywordSearchReturnsEmptyWhenNoMatch**
   - Given: Products that don't match keyword
   - When: Search with keyword
   - Then: Returns empty page

3. **emptyKeywordReturnsAllProducts**
   - Given: Multiple products
   - When: Search with null/empty keyword
   - Then: Returns all products

#### Sort Tests
4. **sortByLatestReturnsNewestFirst**
   - Given: Products with different creation dates
   - When: Search with sort=LATEST
   - Then: Newest products first

5. **sortByPriceLowReturnsLowestFirst**
   - Given: Products with different prices
   - When: Search with sort=PRICE_LOW
   - Then: Lowest price products first

6. **sortByPriceHighReturnsHighestFirst**
   - Given: Products with different prices
   - When: Search with sort=PRICE_HIGH
   - Then: Highest price products first

7. **sortByNameAscReturnsSortedByName**
   - Given: Products with different names
   - When: Search with sort=NAME_ASC
   - Then: Products sorted alphabetically

8. **defaultSortIsLatest**
   - Given: Products with different dates
   - When: Search without sort parameter
   - Then: Defaults to LATEST (newest first)

#### Combined Filter Tests
9. **keywordWithCategoryFilter**
   - Given: Products in different categories
   - When: Search with keyword + categoryId
   - Then: Returns matching products in category only

10. **keywordWithPriceRange**
    - Given: Products with different prices
    - When: Search with keyword + minPrice + maxPrice
    - Then: Returns matching products in price range

## Integration Tests (Controller)

1. **searchWithSortParameter**
   - GET `/api/v1/products?keyword=test&sort=PRICE_LOW`
   - Verify sort parameter is properly parsed

2. **searchWithInvalidSort**
   - GET `/api/v1/products?sort=INVALID`
   - Verify graceful error handling

## Test Data

```java
Product A: name="Samsung TV", description="4K UHD TV", price=1500000
Product B: name="LG Monitor", description="Gaming monitor", price=500000
Product C: name="Gaming Keyboard", description="Mechanical keyboard", price=150000
```

## Priority

1. Keyword search tests (core functionality)
2. Sort tests (user experience)
3. Combined filter tests (edge cases)
