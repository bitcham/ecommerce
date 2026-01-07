package platform.ecommerce.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Response DTO for seller reply modification history.
 * Only accessible by SELLER (owner) and ADMIN.
 */
@Builder
public record SellerReplyHistoryResponse(
        Long id,
        Long sellerReplyId,
        String previousContent,
        Long modifiedBy,
        LocalDateTime modifiedAt
) {
}
