package backend.saferent.service;

import backend.saferent.dto.response.chat.ChatResponse;
import backend.saferent.dto.response.chat.MessageResponse;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    ChatResponse getOrCreateChat(UUID tenantId, UUID landlordId, UUID apartmentId);

    List<ChatResponse> getMyChats();

    MessageResponse sendMessage(UUID chatId, String text);

    List<MessageResponse> getMessages(UUID chatId);

    void markAsRead(UUID chatId);

    int getUnreadCount();
}