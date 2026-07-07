package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatOnboardingInitialization;
import com.huly.backend.domain.model.chat.ChatOnboardingPlan;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.port.ChatMemoryPort;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Inicializa las preferencias de conversación (onboarding) la primera vez que un usuario
 * chatea: crea la preferencia con el estado inicial y publica el saludo. Lógica compartida
 * por el flujo de chat y el listado de historial.
 */
@Service
@RequiredArgsConstructor
public class ChatPreferenceInitializationService {

    private final ChatConversationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatConfigRepository chatConfigRepository;

    @Transactional
    public ChatOnboardingInitialization initialize(Long userId, String conversationId) {
        if (preferenceRepository.findByUserId(userId).isPresent()) {
            return ChatOnboardingInitialization.existing();
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        ChatOnboardingPlan plan = resolveOnboardingPlan(user.getName());

        preferenceRepository.save(ChatConversationPreference.begin(userId, plan.initialStatus(), Instant.now()));
        chatMemoryPort.addMessage(
                conversationId,
                ConversationMessage.of(MessageRole.ASSISTANT, plan.greeting()),
                userId);

        return new ChatOnboardingInitialization(true, plan.greeting());
    }

    private ChatOnboardingPlan resolveOnboardingPlan(String registeredName) {
        return chatConfigRepository.findFirst()
                .map(config -> ChatOnboardingPlan.create(
                        config.getPreferredNameQuestionEnabled(),
                        config.getCommunicationStyleQuestionEnabled(),
                        registeredName))
                .orElseGet(() -> ChatOnboardingPlan.create(null, null, registeredName));
    }
}
