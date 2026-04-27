package backend.saferent.mapper;

import backend.saferent.dto.response.chat.ChatResponse;
import backend.saferent.dto.response.chat.MessageResponse;
import backend.saferent.entity.Chat;
import backend.saferent.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public ChatResponse toResponse(Chat chat, String lastMessage,
                                   java.time.LocalDateTime lastMessageAt,
                                   java.util.UUID lastSenderId,
                                   int unreadCount) {
        return ChatResponse.builder()
                .id(chat.getId())
                .tenantId(chat.getTenant().getId())
                .tenantName(chat.getTenant().getName())
                .tenantPhone(chat.getTenant().getPhone())
                .landlordId(chat.getLandlord().getId())
                .landlordName(chat.getLandlord().getName())
                .landlordPhone(chat.getLandlord().getPhone())
                .apartmentId(chat.getApartment().getId())
                .apartmentTitle(chat.getApartment().getTitle())
                .apartmentAddress(chat.getApartment().getAddress())
                .lastMessage(lastMessage)
                .lastMessageAt(lastMessageAt)
                .lastMessageSenderId(lastSenderId)
                .unreadCount(unreadCount)
                .createdAt(chat.getCreatedAt())
                .build();
    }

    public MessageResponse toResponse(Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .chatId(m.getChat().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .text(m.getText())
                .isRead(m.getIsRead())
                .createdAt(m.getCreatedAt())
                .build();
    }
}