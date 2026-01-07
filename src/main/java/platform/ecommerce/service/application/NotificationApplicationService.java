package platform.ecommerce.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import platform.ecommerce.domain.notification.NotificationType;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.notification.NotificationResponse;
import platform.ecommerce.service.notification.NotificationService;

import java.util.List;

/**
 * Notification application service.
 * Currently delegates to NotificationService.
 */
@Service
@RequiredArgsConstructor
public class NotificationApplicationService {

    private final NotificationService notificationService;

    public NotificationResponse createNotification(Long memberId, NotificationType type,
                                                   String title, String content, String linkUrl) {
        return notificationService.createNotification(memberId, type, title, content, linkUrl);
    }

    public PageResponse<NotificationResponse> getNotifications(Long memberId, Pageable pageable) {
        return notificationService.getNotifications(memberId, pageable);
    }

    public PageResponse<NotificationResponse> getUnreadNotifications(Long memberId, Pageable pageable) {
        return notificationService.getUnreadNotifications(memberId, pageable);
    }

    public List<NotificationResponse> getRecentNotifications(Long memberId) {
        return notificationService.getRecentNotifications(memberId);
    }

    public long getUnreadCount(Long memberId) {
        return notificationService.getUnreadCount(memberId);
    }

    public void markAsRead(Long notificationId, Long memberId) {
        notificationService.markAsRead(notificationId, memberId);
    }

    public int markAllAsRead(Long memberId) {
        return notificationService.markAllAsRead(memberId);
    }
}
