package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatOnboardingInitialization;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Initializes conversational preferences and stores the first assistant greeting once per user.
 */
@Service
@RequiredArgsConstructor
public class InitializeChatPreferencesUseCase {

    private final ChatConversationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final ChatMemoryPort chatMemoryPort;

    /**
     * Initializes chatbot onboarding when the authenticated user has no preference record.
     *
     * @param userId authenticated user identifier
     * @param conversationId active conversation identifier
     * @return initialization outcome
     */
    @Transactional
    public ChatOnboardingInitialization execute(Long userId, String conversationId) {
        if (preferenceRepository.findByUserId(userId).isPresent()) {
            return ChatOnboardingInitialization.existing();
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        String greeting = buildGreeting(user.getName());

        preferenceRepository.save(ChatConversationPreference.begin(userId, Instant.now()));
        chatMemoryPort.addMessage(
                conversationId,
                ConversationMessage.of(MessageRole.ASSISTANT, greeting),
                userId);

        return new ChatOnboardingInitialization(true, greeting);
    }

    private String buildGreeting(String registeredName) {
        if (registeredName == null || registeredName.isBlank()) {
            return "Hola, soy Huly, tu asistente en este recorrido. ¿Cómo te gustaría que te llame de ahora en adelante?";
        }
        return "Hola " + registeredName.trim()
                + ", soy Huly, tu asistente en este recorrido. ¿Cómo te gustaría que te llame de ahora en adelante?";
    }
}
