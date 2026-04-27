package backend.saferent.dto.response.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private UUID id;

    private UUID tenantId;
    private String tenantName;
    private String tenantPhone;

    private UUID landlordId;
    private String landlordName;
    private String landlordPhone;

    private UUID apartmentId;
    private String apartmentTitle;
    private String apartmentAddress;

    // Последнее сообщение (для превью в списке чатов)
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private UUID lastMessageSenderId;

    // Количество непрочитанных для текущего пользователя
    private int unreadCount;

    private LocalDateTime createdAt;
}