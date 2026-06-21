package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.exception.ImageValidationUnavailableException;
import com.huly.backend.domain.model.ImageValidationResult;
import com.huly.backend.domain.provider.ImageValidationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
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

    private final ChatClient chatClient;

    public AnthropicImageValidationAdapter(ChatClient chatClient) {
        this.chatClient = chatClient;
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
            
            return chatClient.prompt()
                    .messages(userMessage)
                    .call()
                    .entity(ImageValidationResult.class);
        } catch (ImageValidationUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al validar imagen con IA: {}", e.getMessage(), e);
            throw new ImageValidationUnavailableException("El servicio de validación de imágenes no está disponible", e);
        }
    }

    private String buildPrompt(String title, String description) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Sos un asistente de bienestar personal, cálido y alentador.\n");
        prompt.append("El usuario quiere completar el reto: \"").append(title).append("\"\n");
        if (description != null && !description.isBlank()) {
            prompt.append("Descripción del reto: \"").append(description).append("\"\n");
        }
        prompt.append("\n¿La imagen adjunta muestra que el usuario realizó este reto?\n");
        prompt.append("Si no está relacionada, respondé con un mensaje breve (máx. 15 palabras), amable y motivador en español, ");
        prompt.append("que explique por qué no aplica y sugiera qué tipo de imagen sería ideal.\n");
        prompt.append("Si está relacionada, el reason puede ser un mensaje de aliento corto.\n");
        return prompt.toString();
    }
}
