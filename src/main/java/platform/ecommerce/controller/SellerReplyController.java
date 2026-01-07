package platform.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.dto.request.SellerReplyRequest;
import platform.ecommerce.dto.response.ApiResponse;
import platform.ecommerce.dto.response.SellerReplyHistoryResponse;
import platform.ecommerce.dto.response.SellerReplyResponse;
import platform.ecommerce.security.SecurityUtils;
import platform.ecommerce.service.application.SellerReplyApplicationService;

import java.util.List;

/**
 * REST controller for seller reply operations.
 */
@Tag(name = "Seller Reply", description = "Seller reply management API")
@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/reply")
@RequiredArgsConstructor
public class SellerReplyController {

    private final SellerReplyApplicationService applicationService;

    @Operation(summary = "Create reply", description = "Create a seller reply for a review (SELLER only)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<SellerReplyResponse> createReply(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @Valid @RequestBody SellerReplyRequest request
    ) {
        Long sellerId = SecurityUtils.getCurrentMemberId();
        SellerReplyResponse response = applicationService.createReply(reviewId, sellerId, request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Get reply", description = "Get seller reply for a review (public)")
    @GetMapping
    public ApiResponse<SellerReplyResponse> getReply(
            @Parameter(description = "Review ID") @PathVariable Long reviewId
    ) {
        SellerReplyResponse response = applicationService.getReply(reviewId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Update reply", description = "Update seller reply (SELLER owner only)")
    @PutMapping
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<SellerReplyResponse> updateReply(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @Valid @RequestBody SellerReplyRequest request
    ) {
        Long sellerId = SecurityUtils.getCurrentMemberId();
        SellerReplyResponse response = applicationService.updateReply(reviewId, sellerId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Delete reply", description = "Delete seller reply (SELLER owner only)")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SELLER')")
    public void deleteReply(
            @Parameter(description = "Review ID") @PathVariable Long reviewId
    ) {
        Long sellerId = SecurityUtils.getCurrentMemberId();
        applicationService.deleteReply(reviewId, sellerId);
    }

    @Operation(summary = "Get reply history", description = "Get modification history (SELLER owner or ADMIN)")
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ApiResponse<List<SellerReplyHistoryResponse>> getHistory(
            @Parameter(description = "Review ID") @PathVariable Long reviewId
    ) {
        List<SellerReplyHistoryResponse> history;

        if (SecurityUtils.hasRole("ADMIN")) {
            // ADMIN can access any history
            history = applicationService.getHistoryByAdmin(reviewId);
        } else {
            // SELLER can only access their own product's review history
            Long sellerId = SecurityUtils.getCurrentMemberId();
            history = applicationService.getHistoryBySeller(reviewId, sellerId);
        }

        return ApiResponse.success(history);
    }
}
