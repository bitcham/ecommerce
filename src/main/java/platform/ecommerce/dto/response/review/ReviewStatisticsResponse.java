package platform.ecommerce.dto.response.review;

import java.util.Map;

public record ReviewStatisticsResponse(
        Long productId,
        long totalReviews,
        double averageRating,
        Map<Integer, Long> ratingDistribution,
        long verifiedPurchaseCount
) {
    public static ReviewStatisticsResponse of(
            Long productId,
            long totalReviews,
            double averageRating,
            Map<Integer, Long> ratingDistribution,
            long verifiedPurchaseCount
    ) {
        return new ReviewStatisticsResponse(
                productId, totalReviews, averageRating, ratingDistribution, verifiedPurchaseCount
        );
    }
}
