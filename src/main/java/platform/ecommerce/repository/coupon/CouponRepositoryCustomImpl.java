package platform.ecommerce.repository.coupon;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import platform.ecommerce.domain.coupon.Coupon;
import platform.ecommerce.domain.coupon.QCoupon;
import platform.ecommerce.dto.request.coupon.CouponSearchCondition;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Custom repository implementation for Coupon queries.
 */
@RequiredArgsConstructor
public class CouponRepositoryCustomImpl implements CouponRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Coupon> searchCoupons(CouponSearchCondition condition, Pageable pageable) {
        QCoupon coupon = QCoupon.coupon;

        List<Coupon> content = queryFactory
                .selectFrom(coupon)
                .where(
                        notDeleted(coupon),
                        activeOnly(coupon, condition.activeOnly()),
                        codeContains(coupon, condition.codeContains())
                )
                .orderBy(coupon.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(coupon.count())
                .from(coupon)
                .where(
                        notDeleted(coupon),
                        activeOnly(coupon, condition.activeOnly()),
                        codeContains(coupon, condition.codeContains())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression notDeleted(QCoupon coupon) {
        return coupon.deletedAt.isNull();
    }

    private BooleanExpression activeOnly(QCoupon coupon, Boolean activeOnly) {
        if (activeOnly == null || !activeOnly) {
            return null;
        }
        return coupon.active.isTrue()
                .and(coupon.validTo.after(LocalDateTime.now()));
    }

    private BooleanExpression codeContains(QCoupon coupon, String codeContains) {
        if (codeContains == null || codeContains.isBlank()) {
            return null;
        }
        return coupon.code.containsIgnoreCase(codeContains);
    }
}
