package com.huly.backend.domain.model.chat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatUserIntentTest {

    private String message;

    @Test
    @DisplayName("detect devuelve NONE cuando el mensaje es nulo")
    void detectShouldReturnNoneWhenMessageIsNull() {
        givenMessage(null);

        ChatUserIntent result = detect();

        thenIntentIs(result, ChatUserIntent.NONE);
    }

    @Test
    @DisplayName("detect devuelve NONE cuando el mensaje queda en blanco tras normalizar")
    void detectShouldReturnNoneWhenMessageIsBlank() {
        givenMessage("   ");

        ChatUserIntent result = detect();

        thenIntentIs(result, ChatUserIntent.NONE);
    }

    @Test
    @DisplayName("detect devuelve CHALLENGE_REQUEST cuando piden explícitamente un reto")
    void detectShouldReturnChallengeRequestWhenAsksForChallenge() {
        givenMessage("Quiero un reto");

        ChatUserIntent result = detect();

        thenIntentIs(result, ChatUserIntent.CHALLENGE_REQUEST);
    }

    @Test
    @DisplayName("detect devuelve ACTIVITY_RECOMMENDATION_REQUEST cuando piden una actividad con un marcador de pedido")
    void detectShouldReturnActivityRecommendationWhenAsksForActivity() {
        givenMessage("Quiero una actividad");

        ChatUserIntent result = detect();

        thenIntentIs(result, ChatUserIntent.ACTIVITY_RECOMMENDATION_REQUEST);
    }

    @Test
    @DisplayName("detect devuelve ACTIVITY_RECOMMENDATION_REQUEST para una frase canónica sin término de actividad")
    void detectShouldReturnActivityRecommendationForCanonicalPhrase() {
        givenMessage("Recomendame algo");

        ChatUserIntent result = detect();

        thenIntentIs(result, ChatUserIntent.ACTIVITY_RECOMMENDATION_REQUEST);
    }

    @Test
    @DisplayName("detect devuelve NONE cuando hay un término de actividad pero sin marcador de pedido")
    void detectShouldReturnNoneWhenActivityTermWithoutRequestMarker() {
        givenMessage("Hoy hice una actividad");

        ChatUserIntent result = detect();

        thenIntentIs(result, ChatUserIntent.NONE);
    }

    @Test
    @DisplayName("detect devuelve NONE para un mensaje conversacional sin intención explícita")
    void detectShouldReturnNoneForPlainMessage() {
        givenMessage("Hola, ¿cómo estás?");

        ChatUserIntent result = detect();

        thenIntentIs(result, ChatUserIntent.NONE);
    }

    @Test
    @DisplayName("isChallengeResponse devuelve false cuando el mensaje es nulo")
    void isChallengeResponseShouldReturnFalseWhenNull() {
        givenMessage(null);

        boolean result = detectChallengeResponse();

        thenIsNotChallengeResponse(result);
    }

    @Test
    @DisplayName("isChallengeResponse devuelve true cuando el usuario acepta el reto")
    void isChallengeResponseShouldReturnTrueWhenAccepting() {
        givenMessage("Acepto este reto");

        boolean result = detectChallengeResponse();

        thenIsChallengeResponse(result);
    }

    @Test
    @DisplayName("isChallengeResponse devuelve true cuando el usuario rechaza el reto por ahora")
    void isChallengeResponseShouldReturnTrueWhenRejecting() {
        givenMessage("Rechazo este reto por ahora");

        boolean result = detectChallengeResponse();

        thenIsChallengeResponse(result);
    }

    @Test
    @DisplayName("isChallengeResponse devuelve false para cualquier otro mensaje")
    void isChallengeResponseShouldReturnFalseForOtherMessage() {
        givenMessage("hola");

        boolean result = detectChallengeResponse();

        thenIsNotChallengeResponse(result);
    }

    // --- arrange ---

    private void givenMessage(String value) {
        this.message = value;
    }

    // --- act ---

    private ChatUserIntent detect() {
        return ChatUserIntent.detect(message);
    }

    private boolean detectChallengeResponse() {
        return ChatUserIntent.isChallengeResponse(message);
    }

    // --- assert ---

    private void thenIntentIs(ChatUserIntent result, ChatUserIntent expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenIsChallengeResponse(boolean result) {
        assertThat(result).isTrue();
    }

    private void thenIsNotChallengeResponse(boolean result) {
        assertThat(result).isFalse();
    }
}
