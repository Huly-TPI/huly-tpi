package com.huly.backend.infrastructure.adapter.anthropic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.provider.LLMChatPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Primary
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "anthropic")
public class AnthropicChatAdapter implements LLMChatPort {

    private final ChatClient chatClient;

    public AnthropicChatAdapter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    private record ChatReplyDto(
            @JsonProperty("huly_reply") String hulyReply,
            @JsonProperty("detected_emotion") String detectedEmotion,
            @JsonProperty("intensity") Integer intensity,
            @JsonProperty("risk_detected") Boolean riskDetected,
            @JsonProperty("matched_word") String matchedWord,
            @JsonProperty("generated_challenge") ChatReply.GeneratedChallenge generatedChallenge
    ) {
        public ChatReply toDomain() {
            EmotionType emotion = null;
            if (detectedEmotion != null && !detectedEmotion.isBlank()) {
                try {
                    emotion = EmotionType.valueOf(detectedEmotion.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
            return new ChatReply(
                    hulyReply,
                    emotion,
                    intensity,
                    riskDetected,
                    matchedWord,
                    null,
                    generatedChallenge
            );
        }
    }

    @Override
    public ChatReply chat(String systemPrompt, String userMessage, List<ConversationMessage> history) {
        try {
            List<Message> messages = new ArrayList<>();
            for (ConversationMessage cm : history != null ? history : List.<ConversationMessage>of()) {
                switch (cm.role()) {
                    case USER -> messages.add(new UserMessage(cm.content()));
                    case ASSISTANT -> messages.add(new AssistantMessage(cm.content()));
                }
            }

            ChatReplyDto dto = chatClient.prompt()
                    .system(systemPrompt)
                    .messages(messages)
                    .user(userMessage)
                    .call()
                    .entity(ChatReplyDto.class);

            return dto != null ? dto.toDomain() : ChatReply.of("No pude generar una respuesta.");
        } catch (Exception e) {
            log.warn("Error generando o parseando respuesta del LLM: {}", e.getMessage());
            return ChatReply.of("Disculpa, estoy teniendo problemas. ¿Podemos intentarlo de nuevo?");
        }

        messages.add(new UserMessage(userMessage
                + "\n\n[FORMATO OBLIGATORIO: Tu respuesta debe ser ÚNICAMENTE el objeto JSON válido. "
                + "Sin texto introductorio, sin explicaciones, sin markdown. "
                + "Comienza directamente con { y termina con }.]"));
        return messages;
    }
}
