package platform.ecommerce.dto.response.notification;

import lombok.Builder;
import platform.ecommerce.domain.notification.Notification;
import platform.ecommerce.domain.notification.NotificationType;

import java.time.LocalDateTime;

/**
 * Notification response DTO.
 */
@Builder
public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String content,
        String linkUrl,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .linkUrl(notification.getLinkUrl())
                .read(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
