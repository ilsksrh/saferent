package backend.saferent.service.impl;

import backend.saferent.dto.response.chat.ChatResponse;
import backend.saferent.dto.response.chat.MessageResponse;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.Chat;
import backend.saferent.entity.Message;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.NotificationType;
import backend.saferent.exception.BadRequestException;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.ChatMapper;
import backend.saferent.repository.ApartmentRepository;
import backend.saferent.repository.ChatRepository;
import backend.saferent.repository.MessageRepository;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.ChatService;
import backend.saferent.service.NotificationService;
import backend.saferent.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository       chatRepository;
    private final MessageRepository    messageRepository;
    private final UserRepository       userRepository;
    private final ApartmentRepository  apartmentRepository;
    private final ChatMapper           chatMapper;
    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;
    private final SimpMessagingTemplate messagingTemplate;

    // ─── Создать или получить чат ────────────────────────────────────────

    @Override
    @Transactional
    public ChatResponse getOrCreateChat(UUID tenantId,
                                        UUID landlordId,
                                        UUID apartmentId) {

        User tenant = getUserOrThrow(tenantId);
        User landlord = getUserOrThrow(landlordId);
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new NotFoundException("Apartment not found"));

        // Нельзя создать чат с самим собой
        if (tenantId.equals(landlordId)) {
            throw new BadRequestException("Cannot create chat with yourself");
        }

        // Нельзя писать хозяину чужой квартиры
        if (!apartment.getLandlord().getId().equals(landlordId)) {
            throw new BadRequestException(
                    "Landlord is not the owner of this apartment"
            );
        }

        // Если чат уже есть — возвращаем его
        Chat chat = chatRepository
                .findByTenantAndLandlordAndApartment(tenant, landlord, apartment)
                .orElseGet(() -> {
                    Chat newChat = Chat.builder()
                            .tenant(tenant)
                            .landlord(landlord)
                            .apartment(apartment)
                            .build();
                    return chatRepository.save(newChat);
                });

        return buildChatResponse(chat, tenant);
    }

    @Override
    @Transactional
    public ChatResponse getOrCreateDirectChat(UUID landlordId) {
        UUID tenantId = securityUtils.getCurrentUserId();
        if (tenantId.equals(landlordId)) {
            throw new BadRequestException("Cannot create chat with yourself");
        }

        User tenant = getUserOrThrow(tenantId);
        User landlord = getUserOrThrow(landlordId);

        Chat chat = chatRepository
                .findByTenantAndLandlordAndApartmentIsNull(tenant, landlord)
                .orElseGet(() -> chatRepository.save(Chat.builder()
                        .tenant(tenant)
                        .landlord(landlord)
                        .build()));

        return buildChatResponse(chat, tenant);
    }

    // ─── Мои чаты ────────────────────────────────────────────────────────

    @Override
    public List<ChatResponse> getMyChats() {
        UUID userId = securityUtils.getCurrentUserId();
        User user = getUserOrThrow(userId);

        return chatRepository.findAllByUser(user)
                .stream()
                .map(chat -> buildChatResponse(chat, user))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(UUID chatId,
                                       String text) {

        UUID senderId = securityUtils.getCurrentUserId();
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found"));

        User sender = getUserOrThrow(senderId);

        // Только участники чата могут писать
        boolean isMember = chat.getTenant().getId().equals(senderId)
                || chat.getLandlord().getId().equals(senderId);
        if (!isMember) {
            throw new BadRequestException("You are not a member of this chat");
        }

        if (text == null || text.isBlank()) {
            throw new BadRequestException("Message text cannot be empty");
        }

        List<Message> unread = messageRepository.findUnreadMessages(chat, sender);
        if (!unread.isEmpty()) {
            unread.forEach(m -> m.setIsRead(true));
            messageRepository.saveAll(unread);
        }

        // Создаём новое сообщение
        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .text(text.trim())
                .isRead(false)
                .build();

        UUID recipientId = chat.getTenant().getId().equals(senderId)
                ? chat.getLandlord().getId()
                : chat.getTenant().getId();

        notificationService.create(
                recipientId,
                "New message from " + sender.getName(),
                text.length() > 50
                        ? text.substring(0, 50) + "..."
                        : text,
                NotificationType.MESSAGE,
                chat.getId(),
                "CHAT"
        );

        MessageResponse saved = chatMapper.toResponse(messageRepository.save(message));

        messagingTemplate.convertAndSend("/topic/chats/" + chat.getId(), saved);

        return saved;
    }

    // ─── История сообщений ───────────────────────────────────────────────

    @Override
    public List<MessageResponse> getMessages(UUID chatId) {
        UUID requesterId = securityUtils.getCurrentUserId();
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found"));

        // Только участник чата может читать историю
        boolean isMember = chat.getTenant().getId().equals(requesterId)
                || chat.getLandlord().getId().equals(requesterId);
        if (!isMember) {
            throw new BadRequestException("You are not a member of this chat");
        }

        return messageRepository.findByChatOrderByCreatedAtAsc(chat)
                .stream()
                .map(chatMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Пометить как прочитанное ────────────────────────────────────────

    @Override
    @Transactional
    public void markAsRead(UUID chatId) {
        UUID userId = securityUtils.getCurrentUserId();
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found"));

        User user = getUserOrThrow(userId);

        // Помечаем прочитанными все сообщения НЕ от этого пользователя
        List<Message> unread = messageRepository.findUnreadMessages(chat, user);
        unread.forEach(m -> m.setIsRead(true));
        messageRepository.saveAll(unread);
    }

    // ─── Количество непрочитанных ────────────────────────────────────────

    @Override
    public int getUnreadCount() {
        UUID userId = securityUtils.getCurrentUserId();
        User user = getUserOrThrow(userId);
        return messageRepository.countUnreadForUser(user);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    // Собрать ChatResponse с последним сообщением и счётчиком
    private ChatResponse buildChatResponse(Chat chat, User currentUser) {
        // Последнее сообщение
        Message last = messageRepository
                .findTopByChatOrderByCreatedAtDesc(chat)
                .orElse(null);

        // Непрочитанные для текущего пользователя
        int unread = messageRepository
                .findUnreadMessages(chat, currentUser)
                .size();

        return chatMapper.toResponse(
                chat,
                last != null ? last.getText() : null,
                last != null ? last.getCreatedAt() : null,
                last != null ? last.getSender().getId() : null,
                unread
        );
    }
}