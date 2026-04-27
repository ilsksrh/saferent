package backend.saferent.service;

import backend.saferent.dto.response.notification.NotificationResponse;
import backend.saferent.entity.enums.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void create(UUID userId, String title, String message,
                NotificationType type, UUID relatedEntityId,
                String relatedEntityType);

    List<NotificationResponse> getMyNotifications(UUID userId);

    int getUnreadCount(UUID userId);

    void markOneAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);
}