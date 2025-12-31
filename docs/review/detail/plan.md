# Review Module Implementation Plan

## Overview
Product review system allowing members to rate and review purchased products.

## Domain Design

### Entity

#### Review (Aggregate Root)
```java
@Entity
public class Review extends BaseEntity implements SoftDeletable {
    private Long memberId;         // Reviewer
    private Long productId;        // Product being reviewed
    private Long orderItemId;      // Order item (for purchase verification)
    private int rating;            // 1-5 stars
    private String title;          // Review title
    private String content;        // Review body
    private List<String> images;   // Review images
    private int helpfulCount;      // Helpful votes
    private boolean verified;      // Verified purchase

    // Operations
    update(rating, title, content, images)
    incrementHelpfulCount()
    markAsVerified()
}
```

### Business Rules
1. Rating must be 1-5
2. One review per order item (prevent duplicate reviews)
3. Only purchaser can review (verified through orderItemId)
4. Reviews can be edited by owner
5. Soft delete (admin can restore)

## Service Layer

### ReviewService
- createReview(memberId, request): Create new review
- getReview(reviewId): Get single review
- getProductReviews(productId, pageable): Get reviews for product
- getMyReviews(memberId, pageable): Get member's reviews
- updateReview(reviewId, memberId, request): Update review
- deleteReview(reviewId, memberId): Delete review
- markHelpful(reviewId, memberId): Vote review as helpful
- getProductRatingSummary(productId): Get rating distribution

## DTOs

### Request
- ReviewCreateRequest(orderItemId, rating, title, content, images)
- ReviewUpdateRequest(rating, title, content, images)

### Response
- ReviewResponse(id, memberId, productId, rating, title, content, images, helpfulCount, verified, createdAt)
- RatingSummaryResponse(averageRating, totalCount, distribution)
