package platform.ecommerce.repository.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import platform.ecommerce.domain.order.Order;
import platform.ecommerce.dto.request.order.OrderSearchCondition;

/**
 * Custom query repository for Order with QueryDSL support.
 */
public interface OrderQueryRepository {

    Page<Order> search(OrderSearchCondition condition, Pageable pageable);
}
