package com.huly.backend.domain.model.chat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatReplyTest {

    @Test
    @DisplayName("Usa el texto introductorio del reto cuando la respuesta no tiene contenido")
    void withRequestedActionChallengeShouldUseIntroWhenContentIsBlank() {
        ChatReply reply = ChatReply.of("");

        ChatReply result = reply.withRequestedActionChallenge();

        assertThat(result.content()).startsWith("Te propongo un reto simple");
        assertThat(result.generatedChallenge()).isNotNull();
        assertThat(result.generatedChallenge().title()).isEqualTo("Reto de accion pequena");
    }

    @Test
    @DisplayName("Agrega la propuesta de reto al final cuando ya hay contenido")
    void withRequestedActionChallengeShouldAppendWhenContentExists() {
        ChatReply reply = ChatReply.of("Estoy para ayudarte.");

        ChatReply result = reply.withRequestedActionChallenge();

        assertThat(result.content())
                .startsWith("Estoy para ayudarte.")
                .contains("Te propongo este reto");
        assertThat(result.generatedChallenge()).isNotNull();
    }

    @Test
    @DisplayName("Un reto con título es recordable")
    void generatedChallengeShouldBeRememberableWhenTitlePresent() {
        ChatReply.GeneratedChallenge challenge = new ChatReply.GeneratedChallenge("Reto", "Hacé algo");

        assertThat(challenge.isRememberable()).isTrue();
    }

    @Test
    @DisplayName("Un reto sin título o con título en blanco no es recordable")
    void generatedChallengeShouldNotBeRememberableWithoutTitle() {
        assertThat(new ChatReply.GeneratedChallenge(null, "Hacé algo").isRememberable()).isFalse();
        assertThat(new ChatReply.GeneratedChallenge("   ", "Hacé algo").isRememberable()).isFalse();
    }
}
