package platform.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.dto.response.ApiResponse;
import platform.ecommerce.dto.response.admin.DashboardSummaryResponse;
import platform.ecommerce.dto.response.admin.OrderStatisticsResponse;
import platform.ecommerce.dto.response.admin.SalesStatisticsResponse;
import platform.ecommerce.dto.response.order.OrderResponse;
import platform.ecommerce.service.admin.SalesPeriod;
import platform.ecommerce.service.application.AdminDashboardApplicationService;
import platform.ecommerce.service.application.OrderApplicationService;

/**
 * Admin REST controller.
 */
@Tag(name = "Admin", description = "Admin management API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminDashboardApplicationService adminDashboardApplicationService;
    private final OrderApplicationService orderApplicationService;

    // ========== Dashboard ==========

    @Operation(summary = "Get dashboard summary", description = "Get today/week/month statistics")
    @GetMapping("/dashboard/summary")
    public ApiResponse<DashboardSummaryResponse> getDashboardSummary() {
        DashboardSummaryResponse response = adminDashboardApplicationService.getDashboardSummary();
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get sales statistics", description = "Get sales by period (daily/weekly/monthly)")
    @GetMapping("/dashboard/sales")
    public ApiResponse<SalesStatisticsResponse> getSalesStatistics(
            @Parameter(description = "Period type") @RequestParam(defaultValue = "DAILY") SalesPeriod period
    ) {
        SalesStatisticsResponse response = adminDashboardApplicationService.getSalesStatistics(period);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get order statistics", description = "Get order count by status")
    @GetMapping("/dashboard/orders")
    public ApiResponse<OrderStatisticsResponse> getOrderStatistics() {
        OrderStatisticsResponse response = adminDashboardApplicationService.getOrderStatistics();
        return ApiResponse.success(response);
    }

    // ========== Order Management ==========

    @Operation(summary = "Get all orders", description = "Get all orders with pagination")
    @GetMapping("/orders")
    public ApiResponse<Page<OrderResponse>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<OrderResponse> response = orderApplicationService.searchOrders(null, pageable);
        return ApiResponse.success(response);
    }
}
