package platform.ecommerce.service.notification;

import org.springframework.data.domain.Pageable;
import platform.ecommerce.domain.notification.NotificationType;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.notification.NotificationResponse;

import java.util.List;

/**
 * Notification service interface.
 */
public interface NotificationService {

    /**
     * Create a new notification.
     */
    NotificationResponse createNotification(Long memberId, NotificationType type,
                                            String title, String content, String linkUrl);

    /**
     * Get paginated notifications for member.
     */
    PageResponse<NotificationResponse> getNotifications(Long memberId, Pageable pageable);

    /**
     * Get unread notifications for member.
     */
    PageResponse<NotificationResponse> getUnreadNotifications(Long memberId, Pageable pageable);

    /**
     * Get recent notifications (top 10).
     */
    List<NotificationResponse> getRecentNotifications(Long memberId);

    /**
     * Get unread notification count.
     */
    long getUnreadCount(Long memberId);

    /**
     * Mark notification as read.
     */
    void markAsRead(Long notificationId, Long memberId);

    /**
     * Mark all notifications as read.
     */
    int markAllAsRead(Long memberId);

    // ========== Convenience methods for common notifications ==========

    /**
     * Send order status notification.
     */
    void notifyOrderStatusChange(Long memberId, String orderNumber, String status);

    /**
     * Send delivery notification.
     */
    void notifyDeliveryUpdate(Long memberId, String orderNumber, String trackingNumber);

    /**
     * Send coupon notification.
     */
    void notifyCouponIssued(Long memberId, String couponName);

    /**
     * Send review request notification.
     */
    void notifyReviewRequest(Long memberId, String productName, Long orderId);
}
