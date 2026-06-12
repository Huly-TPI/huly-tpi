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
}
