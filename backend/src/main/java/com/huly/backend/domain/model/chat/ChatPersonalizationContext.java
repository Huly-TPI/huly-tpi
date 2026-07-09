package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.CommunicationStyle;
import java.util.List;

/**
 * Contains trusted user personalization data injected into the chatbot prompt.
 *
 * @param registeredName name stored during user registration
 * @param preferredName name selected for chatbot conversations
 * @param communicationStyle selected chatbot communication style
 * @param challengeHistory list of challenges with their acceptance/rejection history
 */
public record ChatPersonalizationContext(
        String registeredName,
        String preferredName,
        CommunicationStyle communicationStyle,
        List<ChallengeHistoryEntry> challengeHistory
) {

    public record ChallengeHistoryEntry(
            String title,
            int acceptedCount,
            int rejectedCount
    ) {}

    public ChatPersonalizationContext(
            String registeredName,
            String preferredName,
            CommunicationStyle communicationStyle) {
        this(registeredName, preferredName, communicationStyle, List.of());
    }

    /**
     * Arma el contexto de personalización a partir del nombre registrado y una preferencia
     * de conversación que puede ser nula.
     */
    public static ChatPersonalizationContext from(
            String registeredName,
            ChatConversationPreference preference) {
        if (preference == null) {
            return new ChatPersonalizationContext(registeredName, null, null, List.of());
        }
        return new ChatPersonalizationContext(
                registeredName,
                preference.getPreferredName(),
                preference.getCommunicationStyle(),
                List.of());
    }

    public static ChatPersonalizationContext from(
            String registeredName,
            ChatConversationPreference preference,
            List<ChallengeHistoryEntry> challengeHistory) {
        if (preference == null) {
            return new ChatPersonalizationContext(registeredName, null, null, challengeHistory);
        }
        return new ChatPersonalizationContext(
                registeredName,
                preference.getPreferredName(),
                preference.getCommunicationStyle(),
                challengeHistory);
    }
}
