package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatOnboardingInitialization;
import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.chat.ChatPreferenceHandlingResult;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.model.enums.ChatPreferenceMessageType;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.service.chat.ChatPreferenceResolutionService;
import com.huly.backend.domain.service.chat.ChatQuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Handles optional conversational onboarding and explicit preference changes.
 */
@Service
@RequiredArgsConstructor
public class HandleChatPreferencesUseCase {

    private static final String STYLE_QUESTION =
            "¿Cómo te gustaría que te hable? Puede ser de forma neutra, amable, informal, "
                    + "formal, directa, indirecta, cercana o como un amigo.";

    private final ChatConversationPreferenceRepository preferenceRepository;
    private final ChatPreferenceResolutionService resolutionService;
    private final InitializeChatPreferencesUseCase initializeChatPreferencesUseCase;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatQuotaService chatQuotaService;
    private final ChatConfigRepository chatConfigRepository;

    @Transactional
    public ChatPreferenceHandlingResult execute(
            Long userId,
            String conversationId,
            String message) {
        Optional<ChatConversationPreference> storedPreference =
                preferenceRepository.findByUserId(userId);
        if (storedPreference.isEmpty()) {
            chatQuotaService.assertWithinLimit(userId);
            ChatOnboardingInitialization initialization =
                    initializeChatPreferencesUseCase.execute(userId, conversationId);
            return Boolean.TRUE.equals(initialization.initialized())
                    ? ChatPreferenceHandlingResult.handled(
                            ChatReply.of(initialization.assistantMessage()))
                    : ChatPreferenceHandlingResult.continueChat();
        }

        ChatConversationPreference preference = storedPreference.get();
        return switch (preference.getOnboardingStatus()) {
            case ASKED_PREFERRED_NAME ->
                    handleExpectedName(preference, conversationId, message);
            case PENDING_COMMUNICATION_STYLE ->
                    handlePendingStyle(preference, conversationId, message);
            case ASKED_COMMUNICATION_STYLE ->
                    handleExpectedStyle(preference, conversationId, message);
            case COMPLETED ->
                    handleCompletedPreferenceChange(preference, conversationId, message);
        };
    }

    private ChatPreferenceHandlingResult handleExpectedName(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        ChatPreferenceDetectionResult detection = resolutionService.resolve(
                message,
                ChatPreferenceExpectedField.PREFERRED_NAME);
        if (!detection.hasPreference()) {
            preferenceRepository.save(preference.complete(Instant.now()));
            return ChatPreferenceHandlingResult.continueChat();
        }

        Instant now = Instant.now();
        ChatConversationPreference updated = preference;
        if (detection.preferredName() != null) {
            updated = updated.withPreferredNamePendingStyle(detection.preferredName(), now);
        }
        if (detection.communicationStyle() != null) {
            updated = updated.withCommunicationStyle(detection.communicationStyle(), now);
        } else if (!communicationStyleQuestionEnabled()) {
            updated = updated.complete(now);
        } else if (detection.messageType() == ChatPreferenceMessageType.PREFERENCE_ONLY) {
            updated = updated.markCommunicationStyleAsked(now);
        }
        preferenceRepository.save(updated);

        if (detection.messageType() == ChatPreferenceMessageType.MIXED) {
            return updated.getOnboardingStatus() == ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE
                    ? ChatPreferenceHandlingResult.continueChatAndOfferStyle()
                    : ChatPreferenceHandlingResult.continueChat();
        }

        String response = buildNameResponse(detection, updated);
        return directReply(preference.getUserId(), conversationId, message, response);
    }

    private ChatPreferenceHandlingResult handlePendingStyle(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        ChatPreferenceDetectionResult detection = resolutionService.resolve(
                message,
                ChatPreferenceExpectedField.ANY);
        if (detection.communicationStyle() == null) {
            return ChatPreferenceHandlingResult.continueChatAndOfferStyle();
        }
        return saveStyleChange(preference, conversationId, message, detection);
    }

    private ChatPreferenceHandlingResult handleExpectedStyle(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        ChatPreferenceDetectionResult detection = resolutionService.resolve(
                message,
                ChatPreferenceExpectedField.COMMUNICATION_STYLE);
        if (detection.communicationStyle() == null) {
            preferenceRepository.save(preference.complete(Instant.now()));
            return ChatPreferenceHandlingResult.continueChat();
        }
        return saveStyleChange(preference, conversationId, message, detection);
    }

    private ChatPreferenceHandlingResult saveStyleChange(
            ChatConversationPreference preference,
            String conversationId,
            String message,
            ChatPreferenceDetectionResult detection) {
        CommunicationStyle style = detection.communicationStyle();
        preferenceRepository.save(preference.withCommunicationStyle(style, Instant.now()));
        if (detection.messageType() == ChatPreferenceMessageType.MIXED) {
            return ChatPreferenceHandlingResult.continueChat();
        }
        String response = "Entendido"
                + preferredNameSuffix(preference)
                + ". Desde ahora voy a hablarte con un estilo " + style.displayName() + ".";
        return directReply(preference.getUserId(), conversationId, message, response);
    }

    private ChatPreferenceHandlingResult handleCompletedPreferenceChange(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        ChatPreferenceDetectionResult detection = resolutionService.resolve(
                message,
                ChatPreferenceExpectedField.ANY);
        if (!detection.hasPreference()) {
            return ChatPreferenceHandlingResult.continueChat();
        }

        ChatConversationPreference updated = preference;
        Instant now = Instant.now();
        if (detection.preferredName() != null) {
            updated = updated.updatePreferredName(detection.preferredName(), now);
        }
        if (detection.communicationStyle() != null) {
            updated = updated.updateCommunicationStyle(detection.communicationStyle(), now);
        }
        preferenceRepository.save(updated);

        if (detection.messageType() == ChatPreferenceMessageType.MIXED) {
            return ChatPreferenceHandlingResult.continueChat();
        }
        return directReply(
                preference.getUserId(),
                conversationId,
                message,
                buildCompletedChangeResponse(detection));
    }

    private ChatPreferenceHandlingResult directReply(
            Long userId,
            String conversationId,
            String userMessage,
            String assistantMessage) {
        chatQuotaService.assertWithinLimit(userId);
        saveExchange(userId, conversationId, userMessage, assistantMessage);
        return ChatPreferenceHandlingResult.handled(ChatReply.of(assistantMessage));
    }

    private String buildNameResponse(
            ChatPreferenceDetectionResult detection,
            ChatConversationPreference preference) {
        if (detection.preferredName() == null) {
            return "Entendido. Desde ahora voy a hablarte con un estilo "
                    + detection.communicationStyle().displayName() + ".";
        }
        String prefix = "Perfecto, " + detection.preferredName() + ".";
        if (detection.communicationStyle() != null) {
            return prefix + " Desde ahora voy a hablarte con un estilo "
                    + detection.communicationStyle().displayName() + ".";
        }
        if (preference.getOnboardingStatus() == ChatOnboardingStatus.ASKED_COMMUNICATION_STYLE) {
            return prefix + " " + STYLE_QUESTION;
        }
        return prefix;
    }

    private String buildCompletedChangeResponse(ChatPreferenceDetectionResult detection) {
        if (detection.preferredName() != null && detection.communicationStyle() != null) {
            return "Listo, de ahora en adelante te voy a decir " + detection.preferredName()
                    + " y voy a hablarte con un estilo "
                    + detection.communicationStyle().displayName() + ".";
        }
        if (detection.preferredName() != null) {
            return "Listo, de ahora en adelante te voy a decir "
                    + detection.preferredName() + ".";
        }
        return "Entendido. Desde ahora voy a hablarte con un estilo "
                + detection.communicationStyle().displayName() + ".";
    }

    private String preferredNameSuffix(ChatConversationPreference preference) {
        String preferredName = preference.getPreferredName();
        return preferredName == null || preferredName.isBlank() ? "" : ", " + preferredName;
    }

    private boolean communicationStyleQuestionEnabled() {
        return chatConfigRepository.findFirst()
                .map(config -> config.getCommunicationStyleQuestionEnabled() == null
                        || config.getCommunicationStyleQuestionEnabled())
                .orElse(true);
    }

    private void saveExchange(
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
    }
}
