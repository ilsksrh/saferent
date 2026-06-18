package backend.saferent.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AssistantChatRequest {

    @NotBlank
    @Size(max = 2000)
    private String message;

    /** Последние реплики диалога (стейт держит фронт). */
    private List<ChatTurn> history = new ArrayList<>();

    /** Язык интерфейса: ru | en | kk. */
    private String lang = "ru";

    @Data
    public static class ChatTurn {
        /** "user" | "assistant" */
        private String role;
        private String text;
    }
}
