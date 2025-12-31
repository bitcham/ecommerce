package platform.ecommerce.repository.product;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import platform.ecommerce.domain.product.Product;
import platform.ecommerce.domain.product.ProductStatus;
import platform.ecommerce.dto.request.product.ProductSearchCondition;
import platform.ecommerce.dto.request.product.ProductSortType;

import java.math.BigDecimal;
import java.util.List;

import static platform.ecommerce.domain.product.QProduct.product;

/**
 * Product QueryDSL repository implementation.
 */
@Repository
@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchProducts(ProductSearchCondition condition, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(
                        nameContains(condition.name()),
                        keywordContains(condition.keyword()),
                        categoryEquals(condition.categoryId()),
                        sellerEquals(condition.sellerId()),
                        statusEquals(condition.status()),
                        priceGoe(condition.minPrice()),
                        priceLoe(condition.maxPrice()),
                        excludeDeleted(condition.excludeDeleted())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(condition.sortType()))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        nameContains(condition.name()),
                        keywordContains(condition.keyword()),
                        categoryEquals(condition.categoryId()),
                        sellerEquals(condition.sellerId()),
                        statusEquals(condition.status()),
                        priceGoe(condition.minPrice()),
                        priceLoe(condition.maxPrice()),
                        excludeDeleted(condition.excludeDeleted())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameContains(String name) {
        return name != null && !name.isBlank() ? product.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        // Search in both name and description (OR condition)
        return product.name.containsIgnoreCase(keyword)
                .or(product.description.containsIgnoreCase(keyword));
    }

    private BooleanExpression categoryEquals(Long categoryId) {
        return categoryId != null ? product.categoryId.eq(categoryId) : null;
    }

    private BooleanExpression sellerEquals(Long sellerId) {
        return sellerId != null ? product.sellerId.eq(sellerId) : null;
    }

    private BooleanExpression statusEquals(ProductStatus status) {
        return status != null ? product.status.eq(status) : null;
    }

    private BooleanExpression priceGoe(BigDecimal minPrice) {
        return minPrice != null ? product.basePrice.goe(minPrice) : null;
    }

    private BooleanExpression priceLoe(BigDecimal maxPrice) {
        return maxPrice != null ? product.basePrice.loe(maxPrice) : null;
    }

    private BooleanExpression excludeDeleted(Boolean exclude) {
        return Boolean.TRUE.equals(exclude) ? product.deletedAt.isNull() : null;
    }

    private OrderSpecifier<?> getOrderSpecifier(ProductSortType sortType) {
        if (sortType == null) {
            return product.createdAt.desc();
        }
        return switch (sortType) {
            case LATEST -> product.createdAt.desc();
            case PRICE_LOW -> product.basePrice.asc();
            case PRICE_HIGH -> product.basePrice.desc();
            case NAME_ASC -> product.name.asc();
        };
    }
}
