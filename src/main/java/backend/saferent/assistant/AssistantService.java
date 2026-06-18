package backend.saferent.assistant;

import backend.saferent.assistant.dto.AssistantChatRequest;
import backend.saferent.assistant.dto.AssistantChatResponse;
import backend.saferent.assistant.llm.LlmClient;
import backend.saferent.assistant.llm.LlmClient.FunctionCall;
import backend.saferent.assistant.llm.LlmClient.FunctionResponse;
import backend.saferent.assistant.llm.LlmClient.LlmResult;
import backend.saferent.assistant.llm.LlmClient.Message;
import backend.saferent.dto.response.apartment.ApartmentResponse;
import backend.saferent.entity.User;
import backend.saferent.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Оркестратор ИИ-консьержа «Жанибек»: строит system-промпт, ведёт цикл
 * function-calling (LLM ↔ инструменты) и собирает ответ. Провайдер LLM скрыт за
 * {@link LlmClient}.
 */
@Service
@RequiredArgsConstructor
public class AssistantService {

    private final LlmClient llm;
    private final AssistantTools tools;
    private final SecurityUtils securityUtils;

    private static final int MAX_TOOL_ITERATIONS = 4;
    private static final Set<String> SUPPORTED_LANGS = Set.of("ru", "en", "kk");

    public AssistantChatResponse chat(AssistantChatRequest req) {
        User user = securityUtils.getCurrentUser();
        String lang = normalizeLang(req.getLang());
        String systemPrompt = buildSystemPrompt(user, lang);

        List<Message> conversation = new ArrayList<>();
        if (req.getHistory() != null) {
            for (AssistantChatRequest.ChatTurn turn : req.getHistory()) {
                if (turn.getText() == null || turn.getText().isBlank()) continue;
                if ("assistant".equalsIgnoreCase(turn.getRole())) {
                    conversation.add(Message.model(turn.getText()));
                } else {
                    conversation.add(Message.user(turn.getText()));
                }
            }
        }
        conversation.add(Message.user(req.getMessage()));

        List<ApartmentResponse> apartments = null;
        String reply = null;

        for (int i = 0; i < MAX_TOOL_ITERATIONS; i++) {
            LlmResult result = llm.chat(systemPrompt, conversation, tools.specs());
            if (result.isToolCall()) {
                FunctionCall call = result.call();
                conversation.add(Message.modelCall(call));
                AssistantTools.ToolOutcome outcome = tools.execute(call.name(), call.args());
                if (outcome.apartments() != null) {
                    apartments = outcome.apartments();
                }
                conversation.add(Message.tool(new FunctionResponse(call.name(), outcome.result())));
            } else {
                reply = result.text();
                break;
            }
        }

        if (reply == null) {
            reply = "Извините, запрос оказался слишком сложным. Попробуйте сформулировать короче.";
        }
        return AssistantChatResponse.builder()
                .reply(reply)
                .apartments(apartments)
                .build();
    }

    private String normalizeLang(String lang) {
        if (lang == null) return "ru";
        String l = lang.toLowerCase().trim();
        return SUPPORTED_LANGS.contains(l) ? l : "ru";
    }

    private String buildSystemPrompt(User user, String lang) {
        String languageName = switch (lang) {
            case "en" -> "English";
            case "kk" -> "Kazakh (қазақша)";
            default -> "Russian (русский)";
        };
        String name = user.getName() != null ? user.getName() : "пользователь";

        return """
                Ты — «Жанибек», дружелюбный ИИ-консьерж платформы аренды жилья SafeRent (Казахстан, Алматы).
                Сейчас с тобой общается %s (роль: %s).

                ТВОИ ЗАДАЧИ — помогать арендатору: искать квартиры, объяснять защиту платежей
                (эскроу, депозит, SafeRent Protection, e-договор, инспекция) и сообщать статус его
                броней, договоров и платежей.

                ПРАВИЛА:
                - Отвечай ИСКЛЮЧИТЕЛЬНО на языке: %s. Не смешивай языки.
                - Используй инструменты для любых фактов. НЕ выдумывай квартиры, цены, даты и суммы —
                  бери их только из результатов инструментов. Если инструмент вернул пусто — так и скажи.
                - Отвечай кратко, тепло и по делу. Цены — в тенге (₸).
                - Оставайся в рамках темы SafeRent и аренды жилья. На посторонние темы вежливо откажись.
                - Ты не даёшь юридических гарантий; при сложных юридических вопросах советуй обратиться к специалисту.
                - Когда показываешь найденные квартиры, не перечисляй их все списком повторно — карточки уже
                  покажутся пользователю; дай короткий комментарий и предложи следующий шаг.
                """.formatted(name, user.getPreferredRole(), languageName);
    }
}
