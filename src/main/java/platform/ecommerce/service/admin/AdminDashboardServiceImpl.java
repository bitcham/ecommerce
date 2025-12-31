package platform.ecommerce.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.dto.response.admin.DashboardSummaryResponse;
import platform.ecommerce.dto.response.admin.OrderStatisticsResponse;
import platform.ecommerce.dto.response.admin.SalesStatisticsResponse;
import platform.ecommerce.repository.MemberRepository;
import platform.ecommerce.repository.order.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin dashboard service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        log.info("Getting dashboard summary");

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // Today stats
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);
        BigDecimal todaySales = getSalesOrZero(orderRepository.sumTotalAmountBetween(startOfToday, endOfToday));
        long todayOrders = orderRepository.countByCreatedAtBetween(startOfToday, endOfToday);
        long todayNewMembers = memberRepository.countByCreatedAtBetween(startOfToday, endOfToday);

        // Week stats (last 7 days)
        LocalDateTime startOfWeek = today.minusDays(6).atStartOfDay();
        BigDecimal weekSales = getSalesOrZero(orderRepository.sumTotalAmountBetween(startOfWeek, endOfToday));
        long weekOrders = orderRepository.countByCreatedAtBetween(startOfWeek, endOfToday);
        long weekNewMembers = memberRepository.countByCreatedAtBetween(startOfWeek, endOfToday);

        // Month stats (last 30 days)
        LocalDateTime startOfMonth = today.minusDays(29).atStartOfDay();
        BigDecimal monthSales = getSalesOrZero(orderRepository.sumTotalAmountBetween(startOfMonth, endOfToday));
        long monthOrders = orderRepository.countByCreatedAtBetween(startOfMonth, endOfToday);
        long monthNewMembers = memberRepository.countByCreatedAtBetween(startOfMonth, endOfToday);

        return DashboardSummaryResponse.builder()
                .todaySales(todaySales)
                .todayOrders(todayOrders)
                .todayNewMembers(todayNewMembers)
                .weekSales(weekSales)
                .weekOrders(weekOrders)
                .weekNewMembers(weekNewMembers)
                .monthSales(monthSales)
                .monthOrders(monthOrders)
                .monthNewMembers(monthNewMembers)
                .build();
    }

    @Override
    public SalesStatisticsResponse getSalesStatistics(SalesPeriod period) {
        log.info("Getting sales statistics for period: {}", period);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from;
        List<SalesStatisticsResponse.SalesDataPoint> data;

        switch (period) {
            case DAILY -> {
                from = now.minusDays(7);
                List<Object[]> dailyData = orderRepository.findDailySalesBetween(from, now);
                data = dailyData.stream()
                        .map(row -> SalesStatisticsResponse.SalesDataPoint.builder()
                                .label(row[0].toString())
                                .amount((BigDecimal) row[1])
                                .build())
                        .collect(Collectors.toList());
            }
            case WEEKLY -> {
                from = now.minusWeeks(4);
                List<Object[]> weeklyData = orderRepository.findWeeklySalesBetween(from, now);
                data = weeklyData.stream()
                        .map(row -> SalesStatisticsResponse.SalesDataPoint.builder()
                                .label("Week " + row[0] + ", " + row[1])
                                .amount((BigDecimal) row[2])
                                .build())
                        .collect(Collectors.toList());
            }
            case MONTHLY -> {
                from = now.minusMonths(12);
                List<Object[]> monthlyData = orderRepository.findMonthlySalesBetween(from, now);
                data = monthlyData.stream()
                        .map(row -> SalesStatisticsResponse.SalesDataPoint.builder()
                                .label(row[1] + "-" + String.format("%02d", row[0]))
                                .amount((BigDecimal) row[2])
                                .build())
                        .collect(Collectors.toList());
            }
            default -> throw new IllegalArgumentException("Unknown period: " + period);
        }

        return SalesStatisticsResponse.builder()
                .period(period)
                .data(data)
                .build();
    }

    @Override
    public OrderStatisticsResponse getOrderStatistics() {
        log.info("Getting order statistics");

        List<Object[]> statusData = orderRepository.countByStatus();
        Map<String, Long> statusCounts = new HashMap<>();
        long total = 0;

        for (Object[] row : statusData) {
            String status = row[0].toString();
            Long count = (Long) row[1];
            statusCounts.put(status, count);
            total += count;
        }

        return OrderStatisticsResponse.builder()
                .statusCounts(statusCounts)
                .totalOrders(total)
                .build();
    }

    private BigDecimal getSalesOrZero(BigDecimal sales) {
        return sales != null ? sales : BigDecimal.ZERO;
    }
}
