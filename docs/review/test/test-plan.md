# Review Module Test Plan

## Domain Tests (ReviewTest.java)

### Review Creation
- [ ] should create review with valid rating
- [ ] should throw exception for rating below 1
- [ ] should throw exception for rating above 5
- [ ] should initialize helpfulCount as 0

### Update
- [ ] should update rating and content
- [ ] should update images
- [ ] should throw exception for invalid rating on update

### Helpful Count
- [ ] should increment helpful count
- [ ] should not go below 0

### Verified Status
- [ ] should mark as verified

### Soft Delete
- [ ] should mark as deleted
- [ ] should restore deleted review

## Service Tests (ReviewServiceTest.java)

### createReview
- [ ] should create review for purchased product
- [ ] should mark as verified for valid purchase
- [ ] should throw exception for duplicate review

### getReview
- [ ] should return review by id
- [ ] should throw exception when not found

### getProductReviews
- [ ] should return paginated reviews for product
- [ ] should filter out deleted reviews

### getMyReviews
- [ ] should return member's reviews

### updateReview
- [ ] should update review
- [ ] should throw exception when not owner

### deleteReview
- [ ] should delete review
- [ ] should throw exception when not owner

### markHelpful
- [ ] should increment helpful count

### getProductRatingSummary
- [ ] should calculate average rating
- [ ] should return rating distribution
