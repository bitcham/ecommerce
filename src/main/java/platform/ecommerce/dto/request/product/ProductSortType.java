package platform.ecommerce.dto.request.product;

/**
 * Product sort options for search.
 */
public enum ProductSortType {
    LATEST,      // 최신순 (default)
    PRICE_LOW,   // 낮은가격순
    PRICE_HIGH,  // 높은가격순
    NAME_ASC     // 이름순
}
