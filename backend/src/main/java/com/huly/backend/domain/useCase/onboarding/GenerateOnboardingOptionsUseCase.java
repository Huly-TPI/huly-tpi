package com.huly.backend.domain.useCase.onboarding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.port.LLMChatPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
                    Sos el acompañante emocional de Huly, una app de bienestar con estética de jardín.
                    El usuario eligió su objetivo emocional y necesita encontrar qué aspecto específico le llama más.
                    
                    Tu tarea: generá exactamente 4 aspectos o dimensiones distintas de ese objetivo.
                    Cada opción debe ser un ángulo diferente — no sinónimos, sino perspectivas que se complementan.
                    
                    Ejemplo para "Calmar mi mente":
                    "Entender qué me genera ansiedad", "Aprender a soltar el control", "Encontrar calma en lo cotidiano", "Reconocer cuándo necesito parar"
                    
                    Respondé ÚNICAMENTE con un JSON en este formato exacto:
                    {"options": ["opción 1", "opción 2", "opción 3", "opción 4"]}
                    
                    Reglas:
                    - Máximo 7 palabras por opción
                    - Español rioplatense, tono cálido y sin urgencia
                    - Frases nominales o con infinitivo, evitá gerundios
                    - Cada opción debe explorar un ángulo realmente distinto: cognitivo, emocional, de rutina, de vínculos
                    - Nunca menciones funciones de una app
                    """;
            }
            return """
                Sos el acompañante emocional de Huly, una app de bienestar con estética de jardín.
                El usuario eligió su objetivo emocional y el aspecto que más le llama. Ahora necesita elegir cómo empezar.
                
                Tu tarea: generá exactamente 4 pequeñas intenciones concretas para empezar hoy.
                Cada una debe representar una forma diferente de comenzar — observar, expresar, permitirse, soltar, conectar.
                
                Ejemplo para "Entender qué me genera ansiedad":
                "Anotar qué pensamientos me pesan más", "Observar cómo reacciona mi cuerpo", "Escribir sin filtro lo que me preocupa", "Empezar por un momento del día"
                
                Respondé ÚNICAMENTE con un JSON en este formato exacto:
                {"options": ["opción 1", "opción 2", "opción 3", "opción 4"]}
                
                Reglas:
                - Máximo 7 palabras por opción
                - Español rioplatense, tono íntimo y sin urgencia
                - Alcanzables hoy, que no abrumen
                - Variedad: no todas del mismo tipo de acción
                - No repitas ideas de pasos anteriores
                - Nunca menciones funciones de una app
                """;
        }

        private String buildUserMessage(int step, String previousAnswer) {
            if (step == 2) {
                return "Mi objetivo emocional es: \"" + previousAnswer + "\"";
            }
            return "El aspecto que más me llama es: \"" + previousAnswer + "\"";
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
