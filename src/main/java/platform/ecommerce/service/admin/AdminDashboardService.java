package platform.ecommerce.service.admin;

import platform.ecommerce.dto.response.admin.DashboardSummaryResponse;
import platform.ecommerce.dto.response.admin.OrderStatisticsResponse;
import platform.ecommerce.dto.response.admin.SalesStatisticsResponse;

/**
 * Admin dashboard service interface.
 */
public interface AdminDashboardService {

    /**
     * Get dashboard summary (today, week, month stats).
     */
    DashboardSummaryResponse getDashboardSummary();

    /**
     * Get sales statistics by period.
     */
    SalesStatisticsResponse getSalesStatistics(SalesPeriod period);

    /**
     * Get order statistics by status.
     */
    OrderStatisticsResponse getOrderStatistics();
}
