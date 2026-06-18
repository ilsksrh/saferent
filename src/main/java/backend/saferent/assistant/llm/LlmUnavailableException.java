package backend.saferent.assistant.llm;

/** Бросается, когда LLM-провайдер не настроен или недоступен (нет ключа, сеть, квота). */
public class LlmUnavailableException extends RuntimeException {
    public LlmUnavailableException(String message) {
        super(message);
    }
}
