package backend.saferent.controller;

import backend.saferent.dto.response.notification.NotificationResponse;
import backend.saferent.service.NotificationService;
import backend.saferent.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Уведомления пользователя")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils       securityUtils;

    @Operation(summary = "Мои уведомления")
    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponse>> getMy() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                notificationService.getMyNotifications(userId)
        );
    }

    @Operation(summary = "Количество непрочитанных")
    @GetMapping("/unread")
    public ResponseEntity<Integer> getUnreadCount() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                notificationService.getUnreadCount(userId)
        );
    }

    @Operation(summary = "Пометить одно уведомление прочитанным")
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<String> markOneAsRead(
            @PathVariable UUID notificationId) {
        UUID userId = securityUtils.getCurrentUserId();
        notificationService.markOneAsRead(notificationId, userId);
        return ResponseEntity.ok("Marked as read");
    }

    @Operation(summary = "Пометить все уведомления прочитанными")
    @PostMapping("/read-all")
    public ResponseEntity<String> markAllAsRead() {
        UUID userId = securityUtils.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All marked as read");
    }
}