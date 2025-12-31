package platform.ecommerce.dto.request.product;

import lombok.Builder;
import platform.ecommerce.domain.product.ProductStatus;

import java.math.BigDecimal;

/**
 * Product search condition for dynamic queries.
 */
@Builder
public record ProductSearchCondition(
        String name,
        String keyword,
        Long categoryId,
        Long sellerId,
        ProductStatus status,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean excludeDeleted,
        ProductSortType sortType
) {
    public ProductSearchCondition {
        if (excludeDeleted == null) {
            excludeDeleted = true;
        }
        if (sortType == null) {
            sortType = ProductSortType.LATEST;
        }
    }

    public static ProductSearchCondition empty() {
        return ProductSearchCondition.builder().build();
    }
}
