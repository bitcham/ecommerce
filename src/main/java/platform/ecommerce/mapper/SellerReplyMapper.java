package platform.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import platform.ecommerce.domain.review.SellerReply;
import platform.ecommerce.domain.review.SellerReplyHistory;
import platform.ecommerce.dto.response.SellerReplyHistoryResponse;
import platform.ecommerce.dto.response.SellerReplyResponse;

/**
 * MapStruct mapper for SellerReply entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SellerReplyMapper {

    /**
     * Convert SellerReply entity to response DTO.
     * Note: isEdited must be set manually based on history count.
     */
    @Mapping(target = "isEdited", ignore = true)
    SellerReplyResponse toResponse(SellerReply reply);

    /**
     * Convert SellerReply entity to response DTO with isEdited flag.
     */
    default SellerReplyResponse toResponse(SellerReply reply, boolean isEdited) {
        return SellerReplyResponse.builder()
                .id(reply.getId())
                .reviewId(reply.getReviewId())
                .sellerId(reply.getSellerId())
                .content(reply.getContent())
                .isEdited(isEdited)
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }

    /**
     * Convert SellerReplyHistory entity to response DTO.
     */
    SellerReplyHistoryResponse toHistoryResponse(SellerReplyHistory history);
}
