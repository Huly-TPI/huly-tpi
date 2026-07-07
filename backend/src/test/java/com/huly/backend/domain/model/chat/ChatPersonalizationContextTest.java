package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.CommunicationStyle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatPersonalizationContextTest {

    @Test
    @DisplayName("Deja las preferencias en null cuando no hay preferencia guardada")
    void fromShouldLeavePreferencesNullWhenPreferenceIsNull() {
        ChatPersonalizationContext context = ChatPersonalizationContext.from("Sergio", null);

        assertThat(context.registeredName()).isEqualTo("Sergio");
        assertThat(context.preferredName()).isNull();
        assertThat(context.communicationStyle()).isNull();
    }

    @Test
    @DisplayName("Toma el nombre preferido y el estilo desde la preferencia guardada")
    void fromShouldMapPreferredNameAndStyleFromPreference() {
        ChatConversationPreference preference = ChatConversationPreference.builder()
                .preferredName("Checho")
                .communicationStyle(CommunicationStyle.DIRECT)
                .build();

        ChatPersonalizationContext context = ChatPersonalizationContext.from("Sergio", preference);

        assertThat(context.registeredName()).isEqualTo("Sergio");
        assertThat(context.preferredName()).isEqualTo("Checho");
        assertThat(context.communicationStyle()).isEqualTo(CommunicationStyle.DIRECT);
    }
}
