package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.provider.EmotionalAnalysisPort;
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
public class AnthropicEmotionalAnalysisAdapter implements EmotionalAnalysisPort {

    private final ChatClient chatClient;

    public AnthropicEmotionalAnalysisAdapter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public EmotionalAnalysisResult analyze(String systemPrompt, String userMessage, List<ConversationMessage> history) {
        try {
            List<Message> messages = new ArrayList<>();
            List<ConversationMessage> safeHistory = history == null ? List.of() : history;
            for (ConversationMessage cm : safeHistory) {
                switch (cm.role()) {
                    case USER -> messages.add(new UserMessage(cm.content()));
                    case ASSISTANT -> messages.add(new AssistantMessage(cm.content()));
                }
            }

            return chatClient.prompt()
                    .system(systemPrompt)
                    .messages(messages)
                    .user(userMessage)
                    .call()
                    .entity(EmotionalAnalysisResult.class);
        } catch (Exception e) {
            log.warn("No se pudo analizar emocionalmente el mensaje, usando fallback neutral: {}", e.getMessage());
            return EmotionalAnalysisResult.neutral();
        }
    }
}
