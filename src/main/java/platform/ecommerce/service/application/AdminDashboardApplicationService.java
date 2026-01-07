package platform.ecommerce.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import platform.ecommerce.dto.response.admin.DashboardSummaryResponse;
import platform.ecommerce.dto.response.admin.OrderStatisticsResponse;
import platform.ecommerce.dto.response.admin.SalesStatisticsResponse;
import platform.ecommerce.service.admin.AdminDashboardService;
import platform.ecommerce.service.admin.SalesPeriod;

/**
 * Admin Dashboard application service.
 * Currently delegates to AdminDashboardService.
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardApplicationService {

    private final AdminDashboardService adminDashboardService;

    public DashboardSummaryResponse getDashboardSummary() {
        return adminDashboardService.getDashboardSummary();
    }

    public SalesStatisticsResponse getSalesStatistics(SalesPeriod period) {
        return adminDashboardService.getSalesStatistics(period);
    }

    public OrderStatisticsResponse getOrderStatistics() {
        return adminDashboardService.getOrderStatistics();
    }
}
