package backend.saferent.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Message text is required")
    @Size(max = 2000, message = "Message too long")
    private String text;
}