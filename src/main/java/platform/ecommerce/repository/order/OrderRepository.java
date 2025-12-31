package platform.ecommerce.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.order.Order;
import platform.ecommerce.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order aggregate root.
 */
public interface OrderRepository extends JpaRepository<Order, Long>, OrderQueryRepository {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByMemberId(Long memberId);

    List<Order> findByMemberIdAndStatus(Long memberId, OrderStatus status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    // ========== Admin Statistics Queries ==========

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt BETWEEN :from AND :to AND o.status != 'CANCELLED'")
    BigDecimal sumTotalAmountBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    long countByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT CAST(o.createdAt AS LocalDate), COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.createdAt BETWEEN :from AND :to AND o.status != 'CANCELLED' " +
           "GROUP BY CAST(o.createdAt AS LocalDate) ORDER BY CAST(o.createdAt AS LocalDate)")
    List<Object[]> findDailySalesBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT FUNCTION('WEEK', o.createdAt), FUNCTION('YEAR', o.createdAt), COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.createdAt BETWEEN :from AND :to AND o.status != 'CANCELLED' " +
           "GROUP BY FUNCTION('WEEK', o.createdAt), FUNCTION('YEAR', o.createdAt) " +
           "ORDER BY FUNCTION('YEAR', o.createdAt), FUNCTION('WEEK', o.createdAt)")
    List<Object[]> findWeeklySalesBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT FUNCTION('MONTH', o.createdAt), FUNCTION('YEAR', o.createdAt), COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.createdAt BETWEEN :from AND :to AND o.status != 'CANCELLED' " +
           "GROUP BY FUNCTION('MONTH', o.createdAt), FUNCTION('YEAR', o.createdAt) " +
           "ORDER BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt)")
    List<Object[]> findMonthlySalesBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countByStatus();
}
