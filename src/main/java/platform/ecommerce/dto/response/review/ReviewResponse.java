package platform.ecommerce.dto.response.review;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Review response DTO.
 */
@Builder
public record ReviewResponse(
        Long id,
        Long memberId,
        Long productId,
        Long orderItemId,
        int rating,
        String title,
        String content,
        List<String> images,
        int helpfulCount,
        boolean verified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
