package backend.saferent.controller;

import backend.saferent.dto.request.chat.CreateChatRequest;
import backend.saferent.dto.request.chat.SendMessageRequest;
import backend.saferent.dto.response.chat.ChatResponse;
import backend.saferent.dto.response.chat.MessageResponse;
import backend.saferent.service.ChatService;
import backend.saferent.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
@Tag(name = "Chat", description = "Чат между арендатором и арендодателем")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final FileStorageService fileStorageService;

    @Operation(summary = "Создать или получить существующий чат")
    @PostMapping
    public ResponseEntity<ChatResponse> getOrCreate(
            @Valid @RequestBody CreateChatRequest request) {
        return ResponseEntity.ok(chatService.getOrCreateChat(
                request.getTenantId(),
                request.getLandlordId(),
                request.getApartmentId()
        ));
    }

    @Operation(summary = "Создать/получить прямой чат с лэндлордом (по ФИО)")
    @PostMapping("/direct")
    public ResponseEntity<ChatResponse> getOrCreateDirect(@RequestParam UUID landlordId) {
        return ResponseEntity.ok(chatService.getOrCreateDirectChat(landlordId));
    }

    @Operation(summary = "Все мои чаты")
    @GetMapping("/my")
    public ResponseEntity<List<ChatResponse>> getMyChats() {
        return ResponseEntity.ok(chatService.getMyChats());
    }

    @Operation(summary = "Отправить сообщение")
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable UUID chatId,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(
                chatService.sendMessage(chatId, request.getText(), null)
        );
    }

    @Operation(summary = "Отправить сообщение с фото (multipart → MinIO)")
    @PostMapping(value = "/{chatId}/messages/image", consumes = "multipart/form-data")
    public ResponseEntity<MessageResponse> sendImageMessage(
            @PathVariable UUID chatId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String text) {
        String url = fileStorageService.upload(file, "chats/" + chatId);
        return ResponseEntity.ok(chatService.sendMessage(chatId, text, url));
    }

    @Operation(summary = "История сообщений")
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable UUID chatId) {
        return ResponseEntity.ok(chatService.getMessages(chatId));
    }

    @Operation(summary = "Пометить все сообщения как прочитанные")
    @PostMapping("/{chatId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable UUID chatId) {
        chatService.markAsRead(chatId);
        return ResponseEntity.ok("Messages marked as read");
    }

    @Operation(summary = "Количество непрочитанных сообщений")
    @GetMapping("/unread")
    public ResponseEntity<Integer> getUnreadCount() {
        return ResponseEntity.ok(chatService.getUnreadCount());
    }
}