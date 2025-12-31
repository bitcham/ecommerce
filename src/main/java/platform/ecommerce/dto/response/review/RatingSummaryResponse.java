package platform.ecommerce.dto.response.review;

import lombok.Builder;

import java.util.Map;

/**
 * Rating summary response DTO.
 */
@Builder
public record RatingSummaryResponse(
        double averageRating,
        int totalCount,
        Map<Integer, Integer> distribution  // rating -> count
) {
}
