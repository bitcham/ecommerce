package platform.ecommerce.dto.response.review;

import platform.ecommerce.domain.review.ReviewImage;

import java.time.LocalDateTime;

public record ReviewImageResponse(
        Long id,
        String imageUrl,
        int displayOrder,
        LocalDateTime createdAt
) {
    public static ReviewImageResponse from(ReviewImage image) {
        return new ReviewImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getDisplayOrder(),
                image.getCreatedAt()
        );
    }
}
