package platform.ecommerce.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.notification.Notification;
import platform.ecommerce.domain.notification.NotificationType;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.notification.NotificationResponse;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.repository.NotificationRepository;

import java.util.List;

/**
 * Notification service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationResponse createNotification(Long memberId, NotificationType type,
                                                   String title, String content, String linkUrl) {
        log.info("Creating notification for member: {}, type: {}", memberId, type);

        Notification notification = Notification.builder()
                .memberId(memberId)
                .type(type)
                .title(title)
                .content(content)
                .linkUrl(linkUrl)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created: id={}", saved.getId());

        return NotificationResponse.from(saved);
    }

    @Override
    public PageResponse<NotificationResponse> getNotifications(Long memberId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
        return PageResponse.of(page.map(NotificationResponse::from));
    }

    @Override
    public PageResponse<NotificationResponse> getUnreadNotifications(Long memberId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByMemberIdAndReadFalseOrderByCreatedAtDesc(memberId, pageable);
        return PageResponse.of(page.map(NotificationResponse::from));
    }

    @Override
    public List<NotificationResponse> getRecentNotifications(Long memberId) {
        return notificationRepository.findTop10ByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    public long getUnreadCount(Long memberId) {
        return notificationRepository.countByMemberIdAndReadFalse(memberId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long memberId) {
        log.info("Marking notification as read: id={}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!notification.getMemberId().equals(memberId)) {
            throw new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        notification.markAsRead();
        log.info("Notification marked as read: id={}", notificationId);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long memberId) {
        log.info("Marking all notifications as read for member: {}", memberId);
        int count = notificationRepository.markAllAsRead(memberId);
        log.info("Marked {} notifications as read", count);
        return count;
    }

    // ========== Convenience methods ==========

    @Override
    @Async
    @Transactional
    public void notifyOrderStatusChange(Long memberId, String orderNumber, String status) {
        String title = "주문 상태 변경";
        String content = String.format("주문번호 %s의 상태가 '%s'(으)로 변경되었습니다.", orderNumber, status);
        String linkUrl = "/orders/" + orderNumber;

        createNotification(memberId, NotificationType.ORDER, title, content, linkUrl);
    }

    @Override
    @Async
    @Transactional
    public void notifyDeliveryUpdate(Long memberId, String orderNumber, String trackingNumber) {
        String title = "배송 시작";
        String content = String.format("주문번호 %s의 상품이 발송되었습니다. 운송장: %s", orderNumber, trackingNumber);
        String linkUrl = "/orders/" + orderNumber;

        createNotification(memberId, NotificationType.DELIVERY, title, content, linkUrl);
    }

    @Override
    @Async
    @Transactional
    public void notifyCouponIssued(Long memberId, String couponName) {
        String title = "쿠폰 발급";
        String content = String.format("'%s' 쿠폰이 발급되었습니다.", couponName);
        String linkUrl = "/my/coupons";

        createNotification(memberId, NotificationType.COUPON, title, content, linkUrl);
    }

    @Override
    @Async
    @Transactional
    public void notifyReviewRequest(Long memberId, String productName, Long orderId) {
        String title = "리뷰 작성 요청";
        String content = String.format("'%s' 상품은 어떠셨나요? 리뷰를 작성해주세요.", productName);
        String linkUrl = "/orders/" + orderId + "/review";

        createNotification(memberId, NotificationType.REVIEW, title, content, linkUrl);
    }
}
