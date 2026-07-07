package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.EmotionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatReplyTest {

    private ChatReply reply;
    private ChatReply.GeneratedChallenge challenge;

    @Test
    @DisplayName("Usa el texto introductorio del reto cuando la respuesta no tiene contenido")
    void withRequestedActionChallengeShouldUseIntroWhenContentIsBlank() {
        givenReply(ChatReply.of(""));

        ChatReply result = requestActionChallenge();

        thenUsesRequestedChallengeIntro(result);
    }

    @Test
    @DisplayName("Usa el texto introductorio del reto cuando el contenido es nulo")
    void withRequestedActionChallengeShouldUseIntroWhenContentIsNull() {
        givenReply(ChatReply.of(null));

        ChatReply result = requestActionChallenge();

        thenUsesRequestedChallengeIntro(result);
    }

    @Test
    @DisplayName("Agrega la propuesta de reto al final cuando ya hay contenido")
    void withRequestedActionChallengeShouldAppendWhenContentExists() {
        givenReply(ChatReply.of("Estoy para ayudarte."));

        ChatReply result = requestActionChallenge();

        thenAppendsRequestedChallengeSuffix(result);
    }

    @Test
    @DisplayName("appendContent usa el texto extra como contenido único cuando el contenido es nulo")
    void appendContentShouldUseExtraAsSoleContentWhenContentIsNull() {
        givenReply(ChatReply.of(null));

        ChatReply result = appendContent("Texto extra");

        thenContentIs(result, "Texto extra");
    }

    @Test
    @DisplayName("appendContent usa el texto extra como contenido único cuando el contenido está en blanco")
    void appendContentShouldUseExtraAsSoleContentWhenContentIsBlank() {
        givenReply(ChatReply.of("   "));

        ChatReply result = appendContent("Texto extra");

        thenContentIs(result, "Texto extra");
    }

    @Test
    @DisplayName("appendContent concatena separando por una línea en blanco cuando ya hay contenido")
    void appendContentShouldConcatenateWhenContentExists() {
        givenReply(ChatReply.of("Hola"));

        ChatReply result = appendContent("Texto extra");

        thenContentIs(result, "Hola\n\nTexto extra");
    }

    @Test
    @DisplayName("El constructor con acción sugerida deja el reto generado en nulo")
    void constructorWithSuggestedActionShouldLeaveGeneratedChallengeNull() {
        ChatReply result = buildWithSuggestedActionConstructor();

        thenHasSuggestedActionAndNoChallenge(result);
    }

    @Test
    @DisplayName("Ofrece la pregunta de estilo cuando no hay riesgo, la intensidad es baja y no hay acción")
    void canOfferCommunicationStyleShouldReturnTrueWhenSafe() {
        givenReplyWithEmotionalState(false, 3);

        boolean result = offerCommunicationStyle();

        thenCanOffer(result);
    }

    @Test
    @DisplayName("Ofrece la pregunta de estilo cuando la intensidad es nula")
    void canOfferCommunicationStyleShouldReturnTrueWhenIntensityIsNull() {
        givenReplyWithEmotionalState(false, null);

        boolean result = offerCommunicationStyle();

        thenCanOffer(result);
    }

    @Test
    @DisplayName("No ofrece la pregunta de estilo cuando se detectó riesgo")
    void canOfferCommunicationStyleShouldReturnFalseWhenRiskDetected() {
        givenReplyWithEmotionalState(true, 3);

        boolean result = offerCommunicationStyle();

        thenCannotOffer(result);
    }

    @Test
    @DisplayName("No ofrece la pregunta de estilo cuando la intensidad emocional es alta")
    void canOfferCommunicationStyleShouldReturnFalseWhenIntensityIsHigh() {
        givenReplyWithEmotionalState(false, 8);

        boolean result = offerCommunicationStyle();

        thenCannotOffer(result);
    }

    @Test
    @DisplayName("No ofrece la pregunta de estilo cuando ya hay una acción sugerida")
    void canOfferCommunicationStyleShouldReturnFalseWhenActionSuggested() {
        givenReplyWithSuggestedAction(false, 3);

        boolean result = offerCommunicationStyle();

        thenCannotOffer(result);
    }

    @Test
    @DisplayName("Un reto con título es recordable")
    void generatedChallengeShouldBeRememberableWhenTitlePresent() {
        givenChallenge(new ChatReply.GeneratedChallenge("Reto", "Hacé algo"));

        boolean result = isRememberable();

        thenRememberable(result);
    }

    @Test
    @DisplayName("Un reto sin título no es recordable")
    void generatedChallengeShouldNotBeRememberableWithNullTitle() {
        givenChallenge(new ChatReply.GeneratedChallenge(null, "Hacé algo"));

        boolean result = isRememberable();

        thenNotRememberable(result);
    }

    @Test
    @DisplayName("Un reto con título en blanco no es recordable")
    void generatedChallengeShouldNotBeRememberableWithBlankTitle() {
        givenChallenge(new ChatReply.GeneratedChallenge("   ", "Hacé algo"));

        boolean result = isRememberable();

        thenNotRememberable(result);
    }

    // --- arrange ---

    private void givenReply(ChatReply value) {
        this.reply = value;
    }

    private void givenReplyWithEmotionalState(Boolean riskDetected, Integer intensity) {
        this.reply = new ChatReply("Hola", null, intensity, riskDetected, null);
    }

    private void givenReplyWithSuggestedAction(Boolean riskDetected, Integer intensity) {
        this.reply = new ChatReply("Hola", null, intensity, riskDetected, null)
                .withSuggestedAction(sampleAction());
    }

    private void givenChallenge(ChatReply.GeneratedChallenge value) {
        this.challenge = value;
    }

    private SuggestedChatAction sampleAction() {
        return new SuggestedChatAction(null, 1L, "Actividad", "Descripción", "https://huly/act/1", 2L);
    }

    // --- act ---

    private ChatReply requestActionChallenge() {
        return reply.withRequestedActionChallenge();
    }

    private ChatReply appendContent(String extra) {
        return reply.appendContent(extra);
    }

    private ChatReply buildWithSuggestedActionConstructor() {
        return new ChatReply("Hola", EmotionType.JOY, 4, false, "palabra", sampleAction());
    }

    private boolean offerCommunicationStyle() {
        return reply.canOfferCommunicationStyle();
    }

    private boolean isRememberable() {
        return challenge.isRememberable();
    }

    // --- assert ---

    private void thenUsesRequestedChallengeIntro(ChatReply result) {
        assertThat(result.content()).startsWith("Te propongo un reto simple");
        assertThat(result.generatedChallenge()).isNotNull();
        assertThat(result.generatedChallenge().title()).isEqualTo("Reto de accion pequena");
    }

    private void thenAppendsRequestedChallengeSuffix(ChatReply result) {
        assertThat(result.content())
                .startsWith("Estoy para ayudarte.")
                .contains("Te propongo este reto");
        assertThat(result.generatedChallenge()).isNotNull();
    }

    private void thenContentIs(ChatReply result, String expected) {
        assertThat(result.content()).isEqualTo(expected);
    }

    private void thenHasSuggestedActionAndNoChallenge(ChatReply result) {
        assertThat(result.content()).isEqualTo("Hola");
        assertThat(result.suggestedAction()).isEqualTo(sampleAction());
        assertThat(result.generatedChallenge()).isNull();
    }

    private void thenCanOffer(boolean result) {
        assertThat(result).isTrue();
    }

    private void thenCannotOffer(boolean result) {
        assertThat(result).isFalse();
    }

    private void thenRememberable(boolean result) {
        assertThat(result).isTrue();
    }

    private void thenNotRememberable(boolean result) {
        assertThat(result).isFalse();
    }
}
