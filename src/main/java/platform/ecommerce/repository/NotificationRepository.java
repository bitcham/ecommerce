package platform.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.notification.Notification;
import platform.ecommerce.domain.notification.NotificationType;

import java.util.List;

/**
 * Repository for notifications.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Page<Notification> findByMemberIdAndReadFalseOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    List<Notification> findTop10ByMemberIdOrderByCreatedAtDesc(Long memberId);

    long countByMemberIdAndReadFalse(Long memberId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.memberId = :memberId AND n.read = false")
    int markAllAsRead(@Param("memberId") Long memberId);

    @Query("SELECT n FROM Notification n WHERE n.memberId = :memberId AND n.type = :type ORDER BY n.createdAt DESC")
    Page<Notification> findByMemberIdAndType(@Param("memberId") Long memberId, @Param("type") NotificationType type, Pageable pageable);
}
