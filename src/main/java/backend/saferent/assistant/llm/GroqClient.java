package backend.saferent.assistant.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация {@link LlmClient} поверх Groq (OpenAI-совместимый
 * {@code /chat/completions} с tool-calling). Активна по умолчанию
 * ({@code ai.provider=groq}); бесплатна и доступна в Казахстане.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "groq", matchIfMissing = true)
public class GroqClient implements LlmClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.groq.api-key:}")
    private String apiKey;

    @Value("${ai.groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${ai.groq.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    public GroqClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public LlmResult chat(String systemPrompt, List<Message> conversation, List<ToolSpec> tools) {
        if (!isConfigured()) {
            throw new LlmUnavailableException("ИИ-ассистент не настроен: отсутствует GROQ_API_KEY.");
        }

        Map<String, Object> body = buildRequest(systemPrompt, conversation, tools);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions", new HttpEntity<>(body, headers), String.class);
            return parseResponse(response.getBody());
        } catch (LlmUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Groq request failed: {}", e.getMessage());
            throw new LlmUnavailableException("ИИ-сервис недоступен. Попробуйте позже.");
        }
    }

    // ---- запрос -------------------------------------------------------------

    private Map<String, Object> buildRequest(String systemPrompt,
                                             List<Message> conversation,
                                             List<ToolSpec> tools) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // OpenAI-протокол требует id у tool_call и его эхо в tool-сообщении.
        // Пары modelCall→tool идут подряд, поэтому связываем по порядку.
        int callSeq = 0;
        String lastToolCallId = null;
        for (Message m : conversation) {
            switch (m.role()) {
                case USER -> messages.add(Map.of("role", "user", "content", safe(m.text())));
                case MODEL -> {
                    if (m.call() != null) {
                        lastToolCallId = "call_" + (callSeq++);
                        Map<String, Object> msg = new LinkedHashMap<>();
                        msg.put("role", "assistant");
                        msg.put("content", "");
                        msg.put("tool_calls", List.of(Map.of(
                                "id", lastToolCallId,
                                "type", "function",
                                "function", Map.of(
                                        "name", m.call().name(),
                                        "arguments", writeJson(nullToEmpty(m.call().args()))))));
                        messages.add(msg);
                    } else {
                        messages.add(Map.of("role", "assistant", "content", safe(m.text())));
                    }
                }
                case TOOL -> {
                    Map<String, Object> msg = new LinkedHashMap<>();
                    msg.put("role", "tool");
                    msg.put("tool_call_id", lastToolCallId != null ? lastToolCallId : "call_0");
                    msg.put("name", m.response().name());
                    msg.put("content", writeJson(nullToEmpty(m.response().result())));
                    messages.add(msg);
                }
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.4);

        if (tools != null && !tools.isEmpty()) {
            List<Map<String, Object>> toolDefs = new ArrayList<>();
            for (ToolSpec t : tools) {
                Map<String, Object> params = (t.parametersSchema() == null || t.parametersSchema().isEmpty())
                        ? Map.of("type", "object", "properties", Map.of())
                        : t.parametersSchema();
                toolDefs.add(Map.of("type", "function", "function", Map.of(
                        "name", t.name(),
                        "description", t.description(),
                        "parameters", params)));
            }
            body.put("tools", toolDefs);
            body.put("tool_choice", "auto");
        }
        return body;
    }

    // ---- ответ --------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private LlmResult parseResponse(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) {
            throw new LlmUnavailableException("ИИ-сервис вернул пустой ответ.");
        }
        JsonNode root;
        try {
            root = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new LlmUnavailableException("ИИ-сервис вернул некорректный ответ.");
        }
        JsonNode message = root.path("choices").path(0).path("message");
        JsonNode toolCalls = message.path("tool_calls");

        if (toolCalls.isArray() && !toolCalls.isEmpty()) {
            JsonNode fn = toolCalls.get(0).path("function");
            String name = fn.path("name").asText();
            String argsJson = fn.path("arguments").asText("");
            Map<String, Object> args = Map.of();
            if (!argsJson.isBlank()) {
                try {
                    args = objectMapper.readValue(argsJson, Map.class);
                } catch (Exception e) {
                    log.warn("Failed to parse tool args '{}': {}", argsJson, e.getMessage());
                }
            }
            return new LlmResult(null, new FunctionCall(name, args));
        }

        String text = message.path("content").asText("").trim();
        if (text.isEmpty()) {
            text = "Извините, не удалось сформировать ответ. Переформулируйте, пожалуйста.";
        }
        return new LlmResult(text, null);
    }

    // ---- helpers ------------------------------------------------------------

    private String writeJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static Map<String, Object> nullToEmpty(Map<String, Object> m) {
        return m == null ? Map.of() : m;
    }
}
