package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatOnboardingInitialization;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.port.ChatMemoryPort;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
public class InitializeChatPreferencesUseCase {

    private final ChatConversationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatConfigRepository chatConfigRepository;

    @Transactional
    public ChatOnboardingInitialization execute(Long userId, String conversationId) {
        if (preferenceRepository.findByUserId(userId).isPresent()) {
            return ChatOnboardingInitialization.existing();
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        ChatConfigFlags config = loadConfig();
        ChatOnboardingStatus initialStatus = initialStatus(config);
        String greeting = buildGreeting(user.getName(), config);

        preferenceRepository.save(ChatConversationPreference.begin(userId, initialStatus, Instant.now()));
        chatMemoryPort.addMessage(
                conversationId,
                ConversationMessage.of(MessageRole.ASSISTANT, greeting),
                userId);

        return new ChatOnboardingInitialization(true, greeting);
    }

    private ChatConfigFlags loadConfig() {
        return chatConfigRepository.findFirst()
                .map(config -> new ChatConfigFlags(
                        enabled(config.getPreferredNameQuestionEnabled()),
                        enabled(config.getCommunicationStyleQuestionEnabled())))
                .orElse(new ChatConfigFlags(true, true));
    }

    private ChatOnboardingStatus initialStatus(ChatConfigFlags config) {
        if (config.askPreferredName()) {
            return ChatOnboardingStatus.ASKED_PREFERRED_NAME;
        }
        if (config.askCommunicationStyle()) {
            return ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE;
        }
        return ChatOnboardingStatus.COMPLETED;
    }

    private String buildGreeting(String registeredName, ChatConfigFlags config) {
        String prefix = registeredName == null || registeredName.isBlank()
                ? "Hola, soy Huly, tu asistente en este recorrido."
                : "Hola " + registeredName.trim() + ", soy Huly, tu asistente en este recorrido.";
        if (config.askPreferredName()) {
            return prefix + " ¿Cómo te gustaría que te llame de ahora en adelante?";
        }
        if (config.askCommunicationStyle()) {
            return prefix + " " + CommunicationStyle.QUESTION_TEXT;
        }
        return prefix + " ¿En qué te puedo ayudar hoy?";
    }

    private boolean enabled(Boolean value) {
        return value == null || value;
    }

    private record ChatConfigFlags(
            boolean askPreferredName,
            boolean askCommunicationStyle) {
    }
}
