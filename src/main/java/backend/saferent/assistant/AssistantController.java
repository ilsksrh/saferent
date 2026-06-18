package backend.saferent.assistant;

import backend.saferent.assistant.dto.AssistantChatRequest;
import backend.saferent.assistant.dto.AssistantChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assistant")
@Tag(name = "Assistant", description = "ИИ-консьерж «Жанибек» для жильцов")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    @Operation(summary = "Сообщение ИИ-консьержу (поиск квартир, FAQ по эскроу, статус аренды)")
    @PostMapping("/chat")
    public ResponseEntity<AssistantChatResponse> chat(@Valid @RequestBody AssistantChatRequest request) {
        return ResponseEntity.ok(assistantService.chat(request));
    }
}
