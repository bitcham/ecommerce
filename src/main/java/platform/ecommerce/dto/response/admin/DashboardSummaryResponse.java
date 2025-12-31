package platform.ecommerce.dto.response.admin;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Dashboard summary response DTO.
 */
@Builder
public record DashboardSummaryResponse(
        BigDecimal todaySales,
        long todayOrders,
        long todayNewMembers,
        BigDecimal weekSales,
        long weekOrders,
        long weekNewMembers,
        BigDecimal monthSales,
        long monthOrders,
        long monthNewMembers
) {
}
