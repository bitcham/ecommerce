package platform.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import platform.ecommerce.dto.response.admin.DashboardSummaryResponse;
import platform.ecommerce.dto.response.admin.SalesStatisticsResponse;
import platform.ecommerce.dto.response.admin.OrderStatisticsResponse;
import platform.ecommerce.repository.MemberRepository;
import platform.ecommerce.repository.order.OrderRepository;
import platform.ecommerce.service.admin.AdminDashboardServiceImpl;
import platform.ecommerce.service.admin.SalesPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for AdminDashboardService.
 */
@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    private static final BigDecimal TODAY_SALES = BigDecimal.valueOf(1500000);
    private static final long TODAY_ORDERS = 25L;
    private static final long TODAY_NEW_MEMBERS = 10L;

    @Nested
    @DisplayName("getDashboardSummary")
    class GetDashboardSummary {

        @Test
        @DisplayName("should return today's summary statistics")
        void returnTodaySummary() {
            // given
            LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
            LocalDateTime endOfToday = startOfToday.plusDays(1);

            given(orderRepository.sumTotalAmountBetween(any(), any())).willReturn(TODAY_SALES);
            given(orderRepository.countByCreatedAtBetween(any(), any())).willReturn(TODAY_ORDERS);
            given(memberRepository.countByCreatedAtBetween(any(), any())).willReturn(TODAY_NEW_MEMBERS);

            // when
            DashboardSummaryResponse response = adminDashboardService.getDashboardSummary();

            // then
            assertThat(response.todaySales()).isEqualByComparingTo(TODAY_SALES);
            assertThat(response.todayOrders()).isEqualTo(TODAY_ORDERS);
            assertThat(response.todayNewMembers()).isEqualTo(TODAY_NEW_MEMBERS);
        }

        @Test
        @DisplayName("should return zero when no data exists")
        void returnZeroWhenNoData() {
            // given
            given(orderRepository.sumTotalAmountBetween(any(), any())).willReturn(null);
            given(orderRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);
            given(memberRepository.countByCreatedAtBetween(any(), any())).willReturn(0L);

            // when
            DashboardSummaryResponse response = adminDashboardService.getDashboardSummary();

            // then
            assertThat(response.todaySales()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.todayOrders()).isZero();
            assertThat(response.todayNewMembers()).isZero();
        }
    }

    @Nested
    @DisplayName("getSalesStatistics")
    class GetSalesStatistics {

        @Test
        @DisplayName("should return daily sales for last 7 days")
        void returnDailySales() {
            // given
            List<Object[]> mockData = new java.util.ArrayList<>();
            mockData.add(new Object[]{LocalDate.now(), BigDecimal.valueOf(100000)});
            mockData.add(new Object[]{LocalDate.now().minusDays(1), BigDecimal.valueOf(150000)});
            given(orderRepository.findDailySalesBetween(any(), any())).willReturn(mockData);

            // when
            SalesStatisticsResponse response = adminDashboardService.getSalesStatistics(SalesPeriod.DAILY);

            // then
            assertThat(response.period()).isEqualTo(SalesPeriod.DAILY);
            assertThat(response.data()).isNotEmpty();
        }

        @Test
        @DisplayName("should return weekly sales for last 4 weeks")
        void returnWeeklySales() {
            // given
            List<Object[]> mockData = new java.util.ArrayList<>();
            mockData.add(new Object[]{1, 2025, BigDecimal.valueOf(500000)});
            given(orderRepository.findWeeklySalesBetween(any(), any())).willReturn(mockData);

            // when
            SalesStatisticsResponse response = adminDashboardService.getSalesStatistics(SalesPeriod.WEEKLY);

            // then
            assertThat(response.period()).isEqualTo(SalesPeriod.WEEKLY);
        }

        @Test
        @DisplayName("should return monthly sales for last 12 months")
        void returnMonthlySales() {
            // given
            List<Object[]> mockData = new java.util.ArrayList<>();
            mockData.add(new Object[]{12, 2024, BigDecimal.valueOf(2000000)});
            given(orderRepository.findMonthlySalesBetween(any(), any())).willReturn(mockData);

            // when
            SalesStatisticsResponse response = adminDashboardService.getSalesStatistics(SalesPeriod.MONTHLY);

            // then
            assertThat(response.period()).isEqualTo(SalesPeriod.MONTHLY);
        }
    }

    @Nested
    @DisplayName("getOrderStatistics")
    class GetOrderStatistics {

        @Test
        @DisplayName("should return order count by status")
        void returnOrderCountByStatus() {
            // given
            List<Object[]> mockData = new java.util.ArrayList<>();
            mockData.add(new Object[]{"PENDING_PAYMENT", 10L});
            mockData.add(new Object[]{"PAID", 25L});
            mockData.add(new Object[]{"SHIPPED", 15L});
            given(orderRepository.countByStatus()).willReturn(mockData);

            // when
            OrderStatisticsResponse response = adminDashboardService.getOrderStatistics();

            // then
            assertThat(response.statusCounts()).containsEntry("PENDING_PAYMENT", 10L);
            assertThat(response.statusCounts()).containsEntry("PAID", 25L);
            assertThat(response.statusCounts()).containsEntry("SHIPPED", 15L);
        }
    }
}
