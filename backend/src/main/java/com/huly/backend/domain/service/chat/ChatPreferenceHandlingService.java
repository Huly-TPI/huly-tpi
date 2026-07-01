package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatOnboardingInitialization;
import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.chat.ChatPreferenceHandlingResult;
import com.huly.backend.domain.model.chat.ChatPreferenceMessage;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.model.enums.ChatPreferenceMessageType;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.port.ChatMemoryPort;
import com.huly.backend.domain.port.ChatPreferenceExtractionPort;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.useCase.chat.InitializeChatPreferencesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatPreferenceHandlingService {

    private final ChatConversationPreferenceRepository preferenceRepository;
    private final ChatPreferenceExtractionPort extractionPort;
    private final InitializeChatPreferencesUseCase initializeChatPreferencesUseCase;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatQuotaService chatQuotaService;
    private final ChatConfigRepository chatConfigRepository;

    @Transactional
    public ChatPreferenceHandlingResult handle(
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
                    handleAskedPreferredName(preference, conversationId, message);
            case PENDING_COMMUNICATION_STYLE ->
                    handlePendingCommunicationStyle(preference, conversationId, message);
            case ASKED_COMMUNICATION_STYLE ->
                    handleAskedCommunicationStyle(preference, conversationId, message);
            case COMPLETED ->
                    handleCompletedPreferenceChange(preference, conversationId, message);
        };
    }

    private ChatPreferenceHandlingResult handleAskedPreferredName(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        ChatPreferenceDetectionResult detection = resolvePreference(
                message,
                ChatPreferenceExpectedField.PREFERRED_NAME);
        if (!detection.hasPreference()) {
            preferenceRepository.save(preference.complete(Instant.now()));
            return ChatPreferenceHandlingResult.continueChat();
        }

        Instant now = Instant.now();
        ChatConversationPreference updated = preference;
        boolean hasPreferredName = detection.preferredName() != null;
        boolean hasCommunicationStyle = detection.communicationStyle() != null;
        boolean mixedMessage = detection.messageType() == ChatPreferenceMessageType.MIXED;
        boolean preferenceOnlyMessage = detection.messageType() == ChatPreferenceMessageType.PREFERENCE_ONLY;

        if (hasPreferredName) {
            updated = updated.withPreferredNamePendingStyle(detection.preferredName(), now);
        }
        if (hasCommunicationStyle) {
            updated = updated.withCommunicationStyle(detection.communicationStyle(), now);
        } else if (!communicationStyleQuestionEnabled()) {
            updated = updated.complete(now);
        } else if (preferenceOnlyMessage) {
            updated = updated.markCommunicationStyleAsked(now);
        }
        preferenceRepository.save(updated);

        if (mixedMessage) {
            return updated.getOnboardingStatus() == ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE
                    ? ChatPreferenceHandlingResult.continueChatAndOfferStyle()
                    : ChatPreferenceHandlingResult.continueChat();
        }

        String response = buildPreferredNameResponse(detection, updated);
        return directReply(preference.getUserId(), conversationId, message, response);
    }

    private ChatPreferenceHandlingResult handlePendingCommunicationStyle(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        ChatPreferenceDetectionResult detection = resolvePreference(
                message,
                ChatPreferenceExpectedField.ANY);
        if (detection.communicationStyle() == null) {
            return ChatPreferenceHandlingResult.continueChatAndOfferStyle();
        }
        return handleDetectedCommunicationStyle(preference, conversationId, message, detection);
    }

    private ChatPreferenceHandlingResult handleAskedCommunicationStyle(
            ChatConversationPreference preference,
            String conversationId,
            String message) {
        ChatPreferenceDetectionResult detection = resolvePreference(
                message,
                ChatPreferenceExpectedField.COMMUNICATION_STYLE);
        if (detection.communicationStyle() == null) {
            preferenceRepository.save(preference.complete(Instant.now()));
            return ChatPreferenceHandlingResult.continueChat();
        }
        return handleDetectedCommunicationStyle(preference, conversationId, message, detection);
    }

    private ChatPreferenceHandlingResult handleDetectedCommunicationStyle(
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
        ChatPreferenceDetectionResult detection = resolvePreference(
                message,
                ChatPreferenceExpectedField.ANY);
        if (!detection.hasPreference()) {
            return ChatPreferenceHandlingResult.continueChat();
        }

        ChatConversationPreference updated = preference;
        Instant now = Instant.now();
        boolean hasPreferredName = detection.preferredName() != null;
        boolean hasCommunicationStyle = detection.communicationStyle() != null;
        boolean mixedMessage = detection.messageType() == ChatPreferenceMessageType.MIXED;

        if (hasPreferredName) {
            updated = updated.updatePreferredName(detection.preferredName(), now);
        }
        if (hasCommunicationStyle) {
            updated = updated.updateCommunicationStyle(detection.communicationStyle(), now);
        }
        preferenceRepository.save(updated);

        if (mixedMessage) {
            return ChatPreferenceHandlingResult.continueChat();
        }
        return directReply(
                preference.getUserId(),
                conversationId,
                message,
                buildPreferenceChangeResponse(detection));
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

    private String buildPreferredNameResponse(
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
            return prefix + " " + CommunicationStyle.QUESTION_TEXT;
        }
        return prefix;
    }

    private String buildPreferenceChangeResponse(ChatPreferenceDetectionResult detection) {
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

    private ChatPreferenceDetectionResult resolvePreference(
            String message,
            ChatPreferenceExpectedField expectedField) {
        if (expectedField != ChatPreferenceExpectedField.ANY) {
            return sanitizedExtraction(message, expectedField);
        }
        if (ChatPreferenceMessage.of(message).hasPreferenceChangeSignal()) {
            return sanitizedExtraction(message, ChatPreferenceExpectedField.ANY);
        }
        return ChatPreferenceDetectionResult.unrelated();
    }

    private ChatPreferenceDetectionResult sanitizedExtraction(
            String message,
            ChatPreferenceExpectedField expectedField) {
        ChatPreferenceDetectionResult semanticResult;
        try {
            semanticResult = extractionPort.extract(message, expectedField);
        } catch (RuntimeException ignored) {
            return ChatPreferenceDetectionResult.unrelated();
        }
        return semanticResult == null
                ? ChatPreferenceDetectionResult.unrelated()
                : semanticResult.sanitized();
    }
}
