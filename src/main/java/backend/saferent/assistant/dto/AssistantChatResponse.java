package backend.saferent.assistant.dto;

import backend.saferent.dto.response.apartment.ApartmentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantChatResponse {

    /** Текстовый ответ ассистента. */
    private String reply;

    /** Если ассистент искал квартиры — карточки для рендера в чате (иначе пусто). */
    private List<ApartmentResponse> apartments;
}
