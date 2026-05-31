package com.huly.backend.domain.useCase.onboarding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.provider.LLMChatPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateOnboardingOptionsUseCase {

    private final LLMChatPort llmChatPort;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> execute(Integer step, String previousAnswer) {
        String prompt = buildSystemPrompt(step);
        String userMessage = buildUserMessage(step, previousAnswer);
        ChatReply reply = llmChatPort.chat(prompt, userMessage, List.of());
        return parseOptions(reply.content());
    }

    private String buildSystemPrompt(int step) {
        if (step == 2) {
            return """
                    Sos un asistente de bienestar mental. El usuario está definiendo su objetivo personal.
                    Basándote en su objetivo inicial, generá exactamente 4 opciones más específicas para explorar.
                    Respondé ÚNICAMENTE con un JSON en este formato exacto:
                    {"options": ["opción 1", "opción 2", "opción 3", "opción 4"]}
                    Las opciones deben ser cortas (máximo 6 palabras), en español rioplatense, cálidas y sin urgencia.
                    """;
        }
        return """
                Sos un asistente de bienestar mental. El usuario está eligiendo cómo empezar su camino.
                Basándote en lo que eligió, generá exactamente 4 formas concretas de comenzar.
                Respondé ÚNICAMENTE con un JSON en este formato exacto:
                {"options": ["opción 1", "opción 2", "opción 3", "opción 4"]}
                Las opciones deben ser acciones concretas (máximo 8 palabras), en español rioplatense, alcanzables y sin urgencia.
                """;
    }

    private String buildUserMessage(int step, String previousAnswer) {
        if (step == 2) {
            return "Mi objetivo es: " + previousAnswer;
        }
        return "Me interesa: " + previousAnswer;
    }

    private List<String> parseOptions(String content) {
        try {
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start == -1 || end == -1 || end <= start) {
                return defaultOptions();

            }
            String json = content.substring(start, end + 1);
            JsonNode node = objectMapper.readTree(json);
            JsonNode optionsNode = node.path("options");
            List<String> options = new ArrayList<>();
            if (optionsNode.isArray()) {
                for (JsonNode optionNode : optionsNode) {
                    options.add(optionNode.asText());
                }
            }
            return options.isEmpty() ? defaultOptions() : options;
        } catch (Exception e) {
            log.error("Error parsing options from LLM response: {}", e.getMessage());
            return defaultOptions();
        }
    }

    private List<String> defaultOptions() {
        return List.of("Explorar tu ritmo", "Empezar de a poco", "Probar algo nuevo", "Seguir como estás");
    }
}
