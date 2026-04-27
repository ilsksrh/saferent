package backend.saferent.repository;

import backend.saferent.entity.Notification;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Все уведомления пользователя (новые сверху)
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Непрочитанные
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    // Количество непрочитанных
    int countByUserAndIsReadFalse(User user);

    // Пометить все прочитанными одним запросом
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true " +
            "WHERE n.user = :user AND n.isRead = false")
    void markAllAsReadForUser(@Param("user") User user);
}