package platform.ecommerce.repository.order;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import platform.ecommerce.domain.order.Order;
import platform.ecommerce.domain.order.OrderStatus;
import platform.ecommerce.domain.order.QOrder;
import platform.ecommerce.dto.request.order.OrderSearchCondition;

import java.time.LocalDateTime;
import java.util.List;

/**
 * QueryDSL implementation for Order queries.
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> search(OrderSearchCondition condition, Pageable pageable) {
        QOrder order = QOrder.order;

        List<Order> content = queryFactory
                .selectFrom(order)
                .where(
                        memberIdEq(condition.memberId()),
                        statusEq(condition.status()),
                        orderNumberContains(condition.orderNumber()),
                        createdAtBetween(condition.startDate(), condition.endDate())
                )
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () ->
                queryFactory
                        .select(order.count())
                        .from(order)
                        .where(
                                memberIdEq(condition.memberId()),
                                statusEq(condition.status()),
                                orderNumberContains(condition.orderNumber()),
                                createdAtBetween(condition.startDate(), condition.endDate())
                        )
                        .fetchOne()
        );
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? QOrder.order.memberId.eq(memberId) : null;
    }

    private BooleanExpression statusEq(OrderStatus status) {
        return status != null ? QOrder.order.status.eq(status) : null;
    }

    private BooleanExpression orderNumberContains(String orderNumber) {
        return orderNumber != null ? QOrder.order.orderNumber.containsIgnoreCase(orderNumber) : null;
    }

    private BooleanExpression createdAtBetween(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            return QOrder.order.createdAt.between(start, end);
        } else if (start != null) {
            return QOrder.order.createdAt.goe(start);
        } else if (end != null) {
            return QOrder.order.createdAt.loe(end);
        }
        return null;
    }
}
