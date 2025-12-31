package platform.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.dto.response.ApiResponse;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.notification.NotificationResponse;
import platform.ecommerce.security.SecurityUtils;
import platform.ecommerce.service.notification.NotificationService;

import java.util.List;

/**
 * Notification REST controller.
 */
@Tag(name = "Notification", description = "Notification management API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get notifications", description = "Get paginated notifications for member")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<NotificationResponse>> getNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PageResponse<NotificationResponse> response = notificationService.getNotifications(memberId, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get unread notifications", description = "Get unread notifications for member")
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<NotificationResponse>> getUnreadNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PageResponse<NotificationResponse> response = notificationService.getUnreadNotifications(memberId, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get recent notifications", description = "Get top 10 recent notifications")
    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<NotificationResponse>> getRecentNotifications() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<NotificationResponse> response = notificationService.getRecentNotifications(memberId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> getUnreadCount() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        long count = notificationService.getUnreadCount(memberId);
        return ApiResponse.success(count);
    }

    @Operation(summary = "Mark as read", description = "Mark notification as read")
    @PostMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(
            @Parameter(description = "Notification ID") @PathVariable Long notificationId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        notificationService.markAsRead(notificationId, memberId);
    }

    @Operation(summary = "Mark all as read", description = "Mark all notifications as read")
    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Integer> markAllAsRead() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        int count = notificationService.markAllAsRead(memberId);
        return ApiResponse.success(count);
    }
}
