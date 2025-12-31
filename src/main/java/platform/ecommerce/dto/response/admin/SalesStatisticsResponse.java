package platform.ecommerce.dto.response.admin;

import lombok.Builder;
import platform.ecommerce.service.admin.SalesPeriod;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sales statistics response DTO.
 */
@Builder
public record SalesStatisticsResponse(
        SalesPeriod period,
        List<SalesDataPoint> data
) {
    @Builder
    public record SalesDataPoint(
            String label,
            BigDecimal amount,
            long orderCount
    ) {}
}
