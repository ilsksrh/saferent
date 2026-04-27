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
public class MessageResponse {

    private UUID id;

    private UUID chatId;

    private UUID senderId;
    private String senderName;

    private String text;
    private Boolean isRead;

    private LocalDateTime createdAt;
}