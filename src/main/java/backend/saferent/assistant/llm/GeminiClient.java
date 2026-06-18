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
 * Реализация {@link LlmClient} поверх Google Gemini REST API
 * ({@code POST {base}/models/{model}:generateContent?key=...}).
 * Использует общий {@code RestTemplate}-бин (см. {@code config.WebConfig}),
 * как и {@code client.AiAnalysisClient}.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiClient implements LlmClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-2.0-flash}")
    private String model;

    @Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    public GeminiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public LlmResult chat(String systemPrompt, List<Message> conversation, List<ToolSpec> tools) {
        if (!isConfigured()) {
            throw new LlmUnavailableException("ИИ-ассистент не настроен: отсутствует GEMINI_API_KEY.");
        }

        Map<String, Object> body = buildRequest(systemPrompt, conversation, tools);
        String url = baseUrl + "/models/" + model + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            return parseResponse(response.getBody());
        } catch (LlmUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini request failed: {}", e.getMessage());
            throw new LlmUnavailableException("ИИ-сервис недоступен. Попробуйте позже.");
        }
    }

    // ---- запрос -------------------------------------------------------------

    private Map<String, Object> buildRequest(String systemPrompt,
                                             List<Message> conversation,
                                             List<ToolSpec> tools) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("system_instruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));

        List<Map<String, Object>> contents = new ArrayList<>();
        for (Message m : conversation) {
            contents.add(toContent(m));
        }
        body.put("contents", contents);

        if (tools != null && !tools.isEmpty()) {
            List<Map<String, Object>> declarations = new ArrayList<>();
            for (ToolSpec t : tools) {
                Map<String, Object> decl = new LinkedHashMap<>();
                decl.put("name", t.name());
                decl.put("description", t.description());
                if (t.parametersSchema() != null && !t.parametersSchema().isEmpty()) {
                    decl.put("parameters", t.parametersSchema());
                }
                declarations.add(decl);
            }
            body.put("tools", List.of(Map.of("function_declarations", declarations)));
        }

        body.put("generationConfig", Map.of("temperature", 0.4));
        return body;
    }

    /** v1beta допускает только роли user/model; functionResponse кладём в user-контент. */
    private Map<String, Object> toContent(Message m) {
        Map<String, Object> content = new LinkedHashMap<>();
        switch (m.role()) {
            case USER -> {
                content.put("role", "user");
                content.put("parts", List.of(Map.of("text", safe(m.text()))));
            }
            case MODEL -> {
                content.put("role", "model");
                if (m.call() != null) {
                    content.put("parts", List.of(Map.of("functionCall",
                            Map.of("name", m.call().name(), "args", nullToEmpty(m.call().args())))));
                } else {
                    content.put("parts", List.of(Map.of("text", safe(m.text()))));
                }
            }
            case TOOL -> {
                content.put("role", "user");
                content.put("parts", List.of(Map.of("functionResponse", Map.of(
                        "name", m.response().name(),
                        "response", nullToEmpty(m.response().result())))));
            }
        }
        return content;
    }

    // ---- ответ --------------------------------------------------------------

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
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        StringBuilder text = new StringBuilder();
        for (JsonNode part : parts) {
            JsonNode fc = part.get("functionCall");
            if (fc != null && fc.has("name")) {
                Map<String, Object> args = objectMapper.convertValue(
                        fc.path("args"), Map.class);
                return new LlmResult(null, new FunctionCall(fc.path("name").asText(),
                        args != null ? args : Map.of()));
            }
            if (part.hasNonNull("text")) {
                text.append(part.path("text").asText());
            }
        }
        String out = text.toString().trim();
        if (out.isEmpty()) {
            out = "Извините, не удалось сформировать ответ. Переформулируйте, пожалуйста.";
        }
        return new LlmResult(out, null);
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static Map<String, Object> nullToEmpty(Map<String, Object> m) {
        return m == null ? Map.of() : m;
    }
}
