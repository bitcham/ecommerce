package platform.ecommerce.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Response DTO for seller reply.
 * Includes isEdited flag to indicate if the reply has been modified.
 */
@Builder
public record SellerReplyResponse(
        Long id,
        Long reviewId,
        Long sellerId,
        String content,
        boolean isEdited,  // true if reply has modification history
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
