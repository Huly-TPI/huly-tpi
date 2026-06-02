package com.huly.backend.domain.useCase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.CloudRecommendation;
import com.huly.backend.domain.provider.LLMChatPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso: analizar los pensamientos sueltos del usuario y recomendar una actividad de bienestar.
 * Usa la IA (LLMChatPort) para clasificar el estado emocional basándose en los pensamientos
 * y devuelve una actividad recomendada (diario, nubes, respiración o burbujas).
 *
 * La IA responde siempre en formato JSON estructurado. Si el parseo falla,
 * se aplica un fallback que recomienda escribir en el diario (actividad más segura y genérica).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetCloudRecommendationUseCase {

    // ObjectMapper compartido (thread-safe) para parsear el JSON que devuelve la IA
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            Eres Huly, un asistente de bienestar mental. El usuario acaba de completar el ejercicio de "nubes emocionales",
            donde escribió pensamientos o emociones que quería soltar.
            Analiza esos pensamientos y recomienda UNA actividad de la siguiente lista:

            - diary: escribir en el diario emocional (redirect_url: /diary)
            - clouds: volver a soltar más pensamientos como nubes (redirect_url: /clouds)
            - breathing: ejercicio de respiración guiada para calmarse (redirect_url: /guided-breathing)
            - bubbles: explotar burbujas para liberar tensión (redirect_url: /bubbles)

            Responde ÚNICAMENTE con un objeto JSON válido con esta estructura exacta, sin texto adicional antes ni después:
            {
              "activity_type": "<diary|clouds|breathing|bubbles>",
              "action_id": "<diary|clouds|breathing|bubbles>",
              "title": "<título corto en español, máximo 6 palabras>",
              "description": "<descripción empática en español, máximo 2 oraciones>",
              "redirect_url": "<url>"
            }
            """;

    private final LLMChatPort llmChatPort;

    public CloudRecommendation execute(List<String> thoughts) {
        // Une los pensamientos con saltos de línea para enviarlos como un solo mensaje a la IA
        String userMessage = String.join("\n", thoughts);
        String raw = null;
        try {
            // Envía los pensamientos a la IA sin historial previo (conversación nueva cada vez)
            raw = llmChatPort.chat(SYSTEM_PROMPT, userMessage, List.of()).content();

            // Extrae el bloque JSON de la respuesta (la IA puede agregar texto extra antes o después)
            String json = extractJson(raw);
            JsonNode node = OBJECT_MAPPER.readTree(json);

            // Valida que el tipo de actividad sea uno de los permitidos; si no, usa "diary" por defecto
            String activityType = node.path("activity_type").asText("diary");
            if (!isValidActivity(activityType)) {
                activityType = "diary";
            }

            String actionId = node.path("action_id").asText(activityType);
            if (!isValidActivity(actionId)) {
                actionId = activityType;
            }

            // La URL de redirección se determina por el tipo de actividad (no se confía en la IA para esto)
            String redirectUrl = switch (activityType) {
                case "clouds" -> "/clouds";
                case "breathing" -> "/guided-breathing";
                case "bubbles" -> "/bubbles";
                default -> "/diary";
            };

            CloudRecommendation recommendation = new CloudRecommendation(
                    activityType,
                    actionId,
                    node.path("title").asText("Escribí en tu diario"),
                    node.path("description").asText("Plasmar lo que sentiste puede ayudarte a procesarlo con más profundidad."),
                    redirectUrl
            );

            return recommendation;
        } catch (Exception e) {
            // Si la IA falla o devuelve JSON inválido, se usa el fallback de diario
            log.warn("Error al procesar recomendación, usando fallback.", e);
            return fallback();
        }
    }

    private boolean isValidActivity(String value) {
        return value.equals("diary") || value.equals("clouds")
                || value.equals("breathing") || value.equals("bubbles");
    }

    /** Extrae el primer bloque JSON válido de un texto (la IA a veces agrega texto antes del JSON). */
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    /** Recomendación por defecto cuando la IA falla: diario emocional (actividad segura y genérica). */
    private CloudRecommendation fallback() {
        return new CloudRecommendation(
                "diary",
                "diary",
                "Escribí en tu diario",
                "Plasmar lo que sentiste puede ayudarte a procesarlo con más profundidad.",
                "/diary"
        );
    }
}
