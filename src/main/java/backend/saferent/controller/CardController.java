package backend.saferent.controller;

import backend.saferent.dto.request.payment.SaveCardRequest;
import backend.saferent.dto.response.payment.SavedCardResponse;
import backend.saferent.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments/cards")
@Tag(name = "Saved Cards", description = "Сохранённые карты (только маска last4)")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @Operation(summary = "Сохранить карту (хранится только last4 + срок)")
    @PostMapping
    public ResponseEntity<SavedCardResponse> addCard(
            @Valid @RequestBody SaveCardRequest request) {
        return ResponseEntity.ok(cardService.addCard(request));
    }

    @Operation(summary = "Мои сохранённые карты")
    @GetMapping("/my")
    public ResponseEntity<List<SavedCardResponse>> getMyCards() {
        return ResponseEntity.ok(cardService.getMyCards());
    }

    @Operation(summary = "Сделать карту картой по умолчанию")
    @PostMapping("/{cardId}/default")
    public ResponseEntity<Void> setDefault(@PathVariable UUID cardId) {
        cardService.setDefault(cardId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить карту")
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }
}
