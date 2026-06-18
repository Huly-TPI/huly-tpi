package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.provider.ChatPreferenceExtractionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "anthropic")
public class AnthropicChatPreferenceExtractionAdapter implements ChatPreferenceExtractionPort {

    private final Resource systemPrompt;
    private final ChatClient chatClient;

    public AnthropicChatPreferenceExtractionAdapter(
            @Value("classpath:/prompts/chat-preference-extraction.st") Resource systemPrompt,
            ChatClient chatClient) {
        this.systemPrompt = systemPrompt;
        this.chatClient = chatClient;
    }

    @Override
    public ChatPreferenceDetectionResult extract(
            String message,
            ChatPreferenceExpectedField expectedField) {
        try {
            String request = "Campo esperado: " + expectedField + "\nMensaje: " + message;
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(request)
                    .call()
                    .entity(ChatPreferenceDetectionResult.class);
        } catch (Exception exception) {
            log.warn("No se pudo extraer la preferencia conversacional: {}", exception.getMessage());
            return ChatPreferenceDetectionResult.unrelated();
        }
    }
}
