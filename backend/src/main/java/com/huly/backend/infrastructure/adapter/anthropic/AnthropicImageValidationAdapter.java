package com.huly.backend.infrastructure.adapter.anthropic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.exception.ImageValidationUnavailableException;
import com.huly.backend.domain.model.ImageValidationResult;
import com.huly.backend.domain.provider.ImageValidationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.util.List;

@Slf4j
@Primary
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "anthropic")
public class AnthropicImageValidationAdapter implements ImageValidationPort {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnthropicImageValidationAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ImageValidationResult validate(byte[] imageBytes, String mimeType, String challengeTitle, String challengeDescription) {
        String prompt = buildPrompt(challengeTitle, challengeDescription);
        try {
            Media media = Media.builder()
                    .mimeType(MimeType.valueOf(mimeType))
                    .data(new ByteArrayResource(imageBytes))
                    .build();
            UserMessage userMessage = UserMessage.builder()
                    .text(prompt)
                    .media(List.of(media))
                    .build();
            ChatResponse response = chatModel.call(new Prompt(List.of(userMessage)));
            String raw = extractText(response);
            return parseResponse(raw);
        } catch (ImageValidationUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al validar imagen con IA: {}", e.getMessage(), e);
            throw new ImageValidationUnavailableException("El servicio de validación de imágenes no está disponible", e);
        }
    }

    private String buildPrompt(String title, String description) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Eres un validador de imágenes para una app de bienestar personal.\n");
        prompt.append("El usuario quiere completar el reto: \"").append(title).append("\"\n");
        if (description != null && !description.isBlank()) {
            prompt.append("Descripción del reto: \"").append(description).append("\"\n");
        }
        prompt.append("\n¿La imagen adjunta está relacionada con este reto?\n");
        prompt.append("Responde SOLO con JSON válido, sin texto adicional:\n");
        prompt.append("{\"valid\": true/false, \"reason\": \"motivo breve en español\"}");
        return prompt.toString();
    }

    private String extractText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        return response.getResult().getOutput().getText();
    }

    private ImageValidationResult parseResponse(String raw) {
        try {
            int start = raw.indexOf('{');
            int end = raw.lastIndexOf('}');
            if (start == -1 || end == -1 || end <= start) {
                throw new IllegalStateException("Respuesta sin JSON: " + raw);
            }
            String json = raw.substring(start, end + 1);
            JsonNode node = objectMapper.readTree(json);
            boolean valid = node.get("valid").asBoolean();
            String reason = node.get("reason").asText();
            return new ImageValidationResult(valid, reason);
        } catch (Exception e) {
            log.error("No se pudo parsear la respuesta de validación: {}", raw, e);
            throw new ImageValidationUnavailableException("Respuesta inválida del servicio de validación", e);
        }
    }
}
