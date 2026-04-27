package backend.saferent.service.impl;

import backend.saferent.dto.response.notification.NotificationResponse;
import backend.saferent.entity.Notification;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.NotificationType;
import backend.saferent.exception.BadRequestException;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.NotificationMapper;
import backend.saferent.repository.NotificationRepository;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository         userRepository;
    private final NotificationMapper     notificationMapper;

    // ─── Создать уведомление ─────────────────────────────────────────────

    @Override
    @Transactional
    public void create(UUID userId,
                       String title,
                       String message,
                       NotificationType type,
                       UUID relatedEntityId,
                       String relatedEntityType) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }


    @Override
    public List<NotificationResponse> getMyNotifications(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    public int getUnreadCount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return notificationRepository.countByUserAndIsReadFalse(user);
    }


    @Override
    @Transactional
    public void markOneAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not your notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }


    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        notificationRepository.markAllAsReadForUser(user);
    }
}