package platform.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.dto.request.review.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.dto.response.review.*;
import platform.ecommerce.security.SecurityUtils;
import platform.ecommerce.service.application.ReviewApplicationService;

/**
 * Review REST controller.
 */
@Tag(name = "Review", description = "Product review API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewApplicationService reviewApplicationService;

    @Operation(summary = "Create review", description = "Create a new product review")
    @PostMapping("/products/{productId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ReviewResponse> createReview(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        ReviewResponse response = reviewApplicationService.createReview(memberId, productId, request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Get review", description = "Get review by ID")
    @GetMapping("/reviews/{reviewId}")
    public ApiResponse<ReviewResponse> getReview(
            @Parameter(description = "Review ID") @PathVariable Long reviewId
    ) {
        ReviewResponse response = reviewApplicationService.getReview(reviewId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get product reviews", description = "Get all reviews for a product")
    @GetMapping("/products/{productId}/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> getProductReviews(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<ReviewResponse> response = reviewApplicationService.getProductReviews(productId, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get my reviews", description = "Get all reviews written by member")
    @GetMapping("/members/me/reviews")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<ReviewResponse>> getMyReviews(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PageResponse<ReviewResponse> response = reviewApplicationService.getMemberReviews(memberId, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get product statistics", description = "Get review statistics for a product")
    @GetMapping("/products/{productId}/reviews/statistics")
    public ApiResponse<ReviewStatisticsResponse> getProductStatistics(
            @Parameter(description = "Product ID") @PathVariable Long productId
    ) {
        ReviewStatisticsResponse response = reviewApplicationService.getProductStatistics(productId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Update review", description = "Update review content")
    @PatchMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ReviewResponse> updateReview(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        ReviewResponse response = reviewApplicationService.updateReview(reviewId, memberId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Delete review", description = "Delete a review")
    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void deleteReview(
            @Parameter(description = "Review ID") @PathVariable Long reviewId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        reviewApplicationService.deleteReview(reviewId, memberId);
    }

    // ========== Image Endpoints ==========

    @Operation(summary = "Add image", description = "Add image to review")
    @PostMapping("/reviews/{reviewId}/images")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ReviewImageResponse> addImage(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @Parameter(description = "Image URL") @RequestParam String imageUrl
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        ReviewImageResponse response = reviewApplicationService.addImage(reviewId, memberId, imageUrl);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Remove image", description = "Remove image from review")
    @DeleteMapping("/reviews/{reviewId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void removeImage(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @Parameter(description = "Image ID") @PathVariable Long imageId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        reviewApplicationService.removeImage(reviewId, imageId, memberId);
    }
}
