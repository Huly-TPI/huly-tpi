package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatOnboardingInitialization;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.service.chat.ChatPreferenceDetectionService;
import com.huly.backend.domain.service.chat.ChatQuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Handles deterministic chatbot onboarding answers and explicit preference changes.
 */
@Service
@RequiredArgsConstructor
public class HandleChatPreferencesUseCase {

    private final ChatConversationPreferenceRepository preferenceRepository;
    private final ChatPreferenceDetectionService detectionService;
    private final InitializeChatPreferencesUseCase initializeChatPreferencesUseCase;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatQuotaService chatQuotaService;

    /**
     * Processes a message as conversational-preference input when applicable.
     *
     * @param userId authenticated user identifier
     * @param conversationId active conversation identifier
     * @param message user message
     * @return handled chatbot reply, or empty when normal chat processing should continue
     */
    @Transactional
    public Optional<ChatReply> execute(Long userId, String conversationId, String message) {
        Optional<ChatConversationPreference> storedPreference = preferenceRepository.findByUserId(userId);
        if (storedPreference.isEmpty()) {
            chatQuotaService.assertWithinLimit(userId);
            ChatOnboardingInitialization initialization =
                    initializeChatPreferencesUseCase.execute(userId, conversationId);
            return Boolean.TRUE.equals(initialization.initialized())
                    ? Optional.of(ChatReply.of(initialization.assistantMessage()))
                    : Optional.empty();
        }

        ChatConversationPreference preference = storedPreference.get();
        if (preference.getOnboardingStatus() == ChatOnboardingStatus.ASKED_PREFERRED_NAME) {
            chatQuotaService.assertWithinLimit(userId);
            return Optional.of(handleOnboardingName(preference, conversationId, message));
        }
        if (preference.getOnboardingStatus() == ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE) {
            chatQuotaService.assertWithinLimit(userId);
            return Optional.of(handleOnboardingStyle(preference, conversationId, message));
        }
        return handleCompletedPreferenceChange(preference, conversationId, message);
    }

    private ChatReply handleOnboardingName(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        Optional<String> preferredName = detectionService.detectPreferredName(message, true);
        if (preferredName.isEmpty()) {
            return saveExchange(
                    preference.getUserId(),
                    conversationId,
                    message,
                    "No llegué a identificar el nombre. Podés responder, por ejemplo, \"Sergio\" o \"Llamame Sergito\".");
        }

        ChatConversationPreference updated =
                preference.withPreferredName(preferredName.get(), Instant.now());
        preferenceRepository.save(updated);
        String response = "Perfecto, " + preferredName.get()
                + ". ¿Cómo te gustaría que te hable? Puede ser de forma neutra, amable, informal, directa, cercana o como un amigo.";
        return saveExchange(preference.getUserId(), conversationId, message, response);
    }

    private ChatReply handleOnboardingStyle(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        Optional<CommunicationStyle> style = detectionService.detectCommunicationStyle(message, true);
        if (style.isEmpty()) {
            return saveExchange(
                    preference.getUserId(),
                    conversationId,
                    message,
                    "No llegué a identificar el estilo. Podés elegir, por ejemplo, neutro, amable, informal, directo, cercano o suave y contenedor.");
        }

        preferenceRepository.save(preference.withCommunicationStyle(style.get(), Instant.now()));
        String preferredName = preference.getPreferredName();
        String response = "Entendido"
                + (preferredName == null || preferredName.isBlank() ? "" : ", " + preferredName)
                + ". Desde ahora voy a hablarte con un estilo " + style.get().displayName() + ".";
        return saveExchange(preference.getUserId(), conversationId, message, response);
    }

    private Optional<ChatReply> handleCompletedPreferenceChange(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        Optional<String> preferredName = detectionService.detectPreferredName(message, false);
        if (preferredName.isPresent()) {
            chatQuotaService.assertWithinLimit(preference.getUserId());
            preferenceRepository.save(preference.updatePreferredName(preferredName.get(), Instant.now()));
            return Optional.of(saveExchange(
                    preference.getUserId(),
                    conversationId,
                    message,
                    "Listo, de ahora en adelante te voy a decir " + preferredName.get() + "."));
        }

        Optional<CommunicationStyle> style = detectionService.detectCommunicationStyle(message, false);
        if (style.isPresent()) {
            chatQuotaService.assertWithinLimit(preference.getUserId());
            preferenceRepository.save(preference.updateCommunicationStyle(style.get(), Instant.now()));
            return Optional.of(saveExchange(
                    preference.getUserId(),
                    conversationId,
                    message,
                    "Entendido. Desde ahora voy a hablarte con un estilo " + style.get().displayName() + "."));
        }
        return Optional.empty();
    }

    private ChatReply saveExchange(
            Long userId,
            String conversationId,
            String userMessage,
            String assistantMessage) {
        chatMemoryPort.addMessage(
                conversationId,
                ConversationMessage.of(MessageRole.USER, userMessage),
                userId);
        chatMemoryPort.addMessage(
                conversationId,
                ConversationMessage.of(MessageRole.ASSISTANT, assistantMessage),
                userId);
        return ChatReply.of(assistantMessage);
    }
}
