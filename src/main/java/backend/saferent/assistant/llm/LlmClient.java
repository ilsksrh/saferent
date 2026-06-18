package backend.saferent.assistant.llm;

import java.util.List;
import java.util.Map;

/**
 * Провайдер-нейтральный интерфейс LLM. Одна реализация — {@link GeminiClient};
 * благодаря интерфейсу провайдера легко заменить (например, на Ollama) без правок
 * оркестрации в AssistantService.
 *
 * Клиент отвечает за ОДИН раунд: по system-промпту, истории сообщений и описанию
 * инструментов возвращает либо текст, либо запрос вызова инструмента. Цикл
 * tool-calling крутит уже {@code AssistantService}.
 */
public interface LlmClient {

    /** true, если провайдер сконфигурирован (есть API-ключ). */
    boolean isConfigured();

    LlmResult chat(String systemPrompt, List<Message> conversation, List<ToolSpec> tools);

    enum Role { USER, MODEL, TOOL }

    /** Ровно одно из text/call/response заполнено в зависимости от роли. */
    record Message(Role role, String text, FunctionCall call, FunctionResponse response) {
        public static Message user(String text) { return new Message(Role.USER, text, null, null); }
        public static Message model(String text) { return new Message(Role.MODEL, text, null, null); }
        public static Message modelCall(FunctionCall call) { return new Message(Role.MODEL, null, call, null); }
        public static Message tool(FunctionResponse response) { return new Message(Role.TOOL, null, null, response); }
    }

    record FunctionCall(String name, Map<String, Object> args) {}

    record FunctionResponse(String name, Map<String, Object> result) {}

    /** Описание инструмента: имя, описание и JSON-схема параметров (OpenAPI-стиль). */
    record ToolSpec(String name, String description, Map<String, Object> parametersSchema) {}

    /** Ровно одно из text/call не null. */
    record LlmResult(String text, FunctionCall call) {
        public boolean isToolCall() { return call != null; }
    }
}
