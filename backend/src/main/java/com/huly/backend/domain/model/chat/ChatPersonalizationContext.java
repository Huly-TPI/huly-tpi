package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.CommunicationStyle;

/**
 * Contains trusted user personalization data injected into the chatbot prompt.
 *
 * @param registeredName name stored during user registration
 * @param preferredName name selected for chatbot conversations
 * @param communicationStyle selected chatbot communication style
 */
public record ChatPersonalizationContext(
        String registeredName,
        String preferredName,
        CommunicationStyle communicationStyle
) {

    /**
     * Arma el contexto de personalización a partir del nombre registrado y una preferencia
     * de conversación que puede ser nula.
     */
    public static ChatPersonalizationContext from(
            String registeredName,
            ChatConversationPreference preference) {
        return new ChatPersonalizationContext(
                registeredName,
                preference != null ? preference.getPreferredName() : null,
                preference != null ? preference.getCommunicationStyle() : null);
    }
}
