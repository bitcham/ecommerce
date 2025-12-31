package platform.ecommerce.dto.response.admin;

import lombok.Builder;

import java.util.Map;

/**
 * Order statistics response DTO.
 */
@Builder
public record OrderStatisticsResponse(
        Map<String, Long> statusCounts,
        long totalOrders
) {
}
