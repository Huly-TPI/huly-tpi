package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.EmotionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatEmotionalRecommendationPolicyTest {

    private static final Long USER_ID = 1L;

    private final ChatEmotionalRecommendationPolicy policy = new ChatEmotionalRecommendationPolicy();

    @Test
    @DisplayName("Mantiene el análisis que ya solicita recomendación")
    void resolveShouldKeepAnalysisThatAlreadyRequestsRecommendation() {
        EmotionalAnalysisResult analysis = analysis(true, EmotionType.ANXIETY, -0.7, 0.8, -0.5, 0.8);

        EmotionalAnalysisResult result = resolveWithoutForce(analysis);

        thenSameAs(result, analysis);
    }

    @Test
    @DisplayName("Suprime la recomendación cuando valencia y dominancia son positivas")
    void resolveShouldSuppressRecommendationWhenValenceAndDominanceArePositive() {
        EmotionalAnalysisResult result =
                resolveWithoutForce(analysis(true, EmotionType.MOTIVATION, 0.6, 0.4, 0.3, 0.5));

        thenDoesNotRecommend(result);
        thenEmotionIs(result, EmotionType.MOTIVATION);
    }

    @Test
    @DisplayName("Suprime la recomendación en estado negativo leve de baja intensidad")
    void resolveShouldSuppressRecommendationForLowIntensityMildNegativeState() {
        EmotionalAnalysisResult result =
                resolveWithoutForce(analysis(true, EmotionType.NEUTRAL, -0.3, 0.3, -0.2, 0.5));

        thenDoesNotRecommend(result);
    }

    @Test
    @DisplayName("Suprime la recomendación cuando la valencia es positiva pero la dominancia no")
    void resolveShouldSuppressRecommendationWhenValenceIsPositiveButDominanceIsNot() {
        EmotionalAnalysisResult result =
                resolveWithoutForce(analysis(true, EmotionType.JOY, 0.6, 0.4, -0.1, 0.5));

        thenDoesNotRecommend(result);
    }

    @Test
    @DisplayName("No suprime la recomendación cuando la dominancia es muy negativa")
    void resolveShouldNotSuppressRecommendationWhenDominanceIsVeryNegative() {
        EmotionalAnalysisResult result =
                resolveWithoutForce(analysis(true, EmotionType.FRUSTRATION, -0.45, 0.3, -0.6, 0.65));

        thenRecommends(result);
    }

    @Test
    @DisplayName("No suprime la recomendación cuando la intensidad es alta")
    void resolveShouldNotSuppressRecommendationWhenIntensityIsHigh() {
        EmotionalAnalysisResult result =
                resolveWithoutForce(analysis(true, EmotionType.NEUTRAL, -0.3, 0.3, -0.2, 0.75));

        thenRecommends(result);
    }

    @Test
    @DisplayName("No suprime la recomendación cuando la valencia es muy negativa")
    void resolveShouldNotSuppressRecommendationWhenValenceIsVeryNegative() {
        EmotionalAnalysisResult result =
                resolveWithoutForce(analysis(true, EmotionType.SADNESS, -0.6, 0.3, -0.2, 0.5));

        thenRecommends(result);
    }

    @Test
    @DisplayName("Fuerza la recomendación ante malestar estructurado alto")
    void resolveShouldForceRecommendationForStructuredHighDistress() {
        EmotionalAnalysisResult result =
                resolveWithoutForce(analysis(false, EmotionType.SADNESS, -0.8, 0.2, -0.6, 0.4));

        thenRecommends(result);
        thenUserGoalContains(result, "tristeza");
        thenIntensityIs(result, 0.65);
    }

    @Test
    @DisplayName("Fuerza la recomendación cuando la intensidad supera el umbral")
    void resolveShouldForceRecommendationWhenIntensityAboveThreshold() {
        EmotionalAnalysisResult result =
                resolveWithoutForce(analysis(false, EmotionType.ANXIETY, -0.2, 0.3, -0.2, 0.7));

        thenRecommends(result);
        thenUserGoalIs(result, "calmarse y bajar la ansiedad");
    }

    @Test
    @DisplayName("Fuerza la recomendación cuando la dominancia cae por debajo del umbral")
    void resolveShouldForceRecommendationWhenDominanceBelowThreshold() {
        EmotionalAnalysisResult result =
                resolveWithoutForce(analysis(false, EmotionType.STRESS, -0.2, 0.3, -0.5, 0.4));

        thenRecommends(result);
        thenUserGoalIs(result, "regular estres y recuperar control");
    }

    @Test
    @DisplayName("Fuerza la recomendación con la meta por defecto de desesperanza")
    void resolveShouldForceRecommendationWithHopelessnessDefaultGoal() {
        EmotionalAnalysisResult result =
                resolveWithoutForce(analysis(false, EmotionType.HOPELESSNESS, -0.8, 0.2, -0.6, 0.4));

        thenRecommends(result);
        thenUserGoalIs(result, "sentirse acompanado y aliviar tristeza profunda");
    }

    @Test
    @DisplayName("Al forzar conserva la meta y la razón provistas por el análisis")
    void resolveShouldKeepProvidedGoalAndReasonWhenForcing() {
        EmotionalAnalysisResult result = resolveWithoutForce(
                analysisWithGoal(false, EmotionType.SADNESS, -0.8, 0.2, -0.6, 0.4,
                        "mi meta personal", "razon puntual"));

        thenRecommends(result);
        thenUserGoalIs(result, "mi meta personal");
        thenShortReasonIs(result, "razon puntual");
    }

    @Test
    @DisplayName("Al forzar usa los valores por defecto cuando la meta provista está en blanco")
    void resolveShouldUseDefaultGoalWhenProvidedGoalIsBlank() {
        EmotionalAnalysisResult result = resolveWithoutForce(
                analysisWithGoal(false, EmotionType.SADNESS, -0.8, 0.2, -0.6, 0.4, "   ", "  "));

        thenUserGoalIs(result, "procesar tristeza y sentirse acompanado");
        thenShortReasonIs(result, "Malestar emocional significativo detectado.");
    }

    @Test
    @DisplayName("No aplica override estructurado cuando la emoción es null")
    void resolveShouldSkipStructuredOverrideWhenEmotionIsNull() {
        EmotionalAnalysisResult analysis = analysis(false, null, -0.8, 0.2, -0.6, 0.4);

        EmotionalAnalysisResult result = resolveWithoutForce(analysis);

        thenSameAs(result, analysis);
    }

    @Test
    @DisplayName("No aplica override estructurado cuando la emoción no es de alto malestar")
    void resolveShouldSkipStructuredOverrideWhenEmotionIsNotHighDistress() {
        EmotionalAnalysisResult analysis = analysis(false, EmotionType.FRUSTRATION, -0.8, 0.2, -0.6, 0.4);

        EmotionalAnalysisResult result = resolveWithoutForce(analysis);

        thenSameAs(result, analysis);
    }

    @Test
    @DisplayName("Mantiene el análisis cuando no hay override estructurado ni conversacional")
    void resolveShouldKeepAnalysisWhenNoStructuredOrConversationOverride() {
        EmotionalAnalysisResult analysis = analysis(false, EmotionType.SADNESS, -0.2, 0.3, -0.2, 0.4);

        EmotionalAnalysisResult result = resolveWithoutForce(analysis);

        thenSameAs(result, analysis);
    }

    @Test
    @DisplayName("Crea un VAD de respaldo desde la metadata conversacional")
    void resolveShouldCreateVadFallbackFromConversationMetadata() {
        EmotionalAnalysisResult result =
                resolveWithReply(neutralAnalysis(), reply(EmotionType.ANXIETY, 8));

        thenRecommends(result);
        thenVadIs(result, -0.75, 0.85, -0.70);
    }

    @Test
    @DisplayName("Devuelve neutral cuando el análisis y la respuesta son null")
    void resolveShouldReturnNeutralWhenAnalysisAndReplyAreNull() {
        EmotionalAnalysisResult result = resolveWithoutForce(null);

        thenDoesNotRecommend(result);
        thenEmotionIs(result, EmotionType.NEUTRAL);
    }

    @Test
    @DisplayName("Devuelve neutral cuando la respuesta no tiene emoción")
    void resolveShouldReturnNeutralWhenReplyEmotionIsNull() {
        EmotionalAnalysisResult result = resolveWithReply(null, reply(null, 8));

        thenDoesNotRecommend(result);
    }

    @Test
    @DisplayName("Devuelve neutral cuando la respuesta no tiene intensidad")
    void resolveShouldReturnNeutralWhenReplyIntensityIsNull() {
        EmotionalAnalysisResult result = resolveWithReply(null, reply(EmotionType.ANXIETY, null));

        thenDoesNotRecommend(result);
    }

    @Test
    @DisplayName("Devuelve neutral cuando la emoción de la respuesta no es de alto malestar")
    void resolveShouldReturnNeutralWhenReplyEmotionIsNotHighDistress() {
        EmotionalAnalysisResult result = resolveWithReply(null, reply(EmotionType.JOY, 8));

        thenDoesNotRecommend(result);
    }

    @Test
    @DisplayName("Devuelve neutral cuando la intensidad de la respuesta es baja")
    void resolveShouldReturnNeutralWhenReplyIntensityBelowThreshold() {
        EmotionalAnalysisResult result = resolveWithReply(null, reply(EmotionType.ANXIETY, 5));

        thenDoesNotRecommend(result);
    }

    @Test
    @DisplayName("Construye el respaldo de duelo/tristeza desde la conversación")
    void resolveShouldBuildGriefFallbackFromConversation() {
        EmotionalAnalysisResult result = resolveWithReply(null, reply(EmotionType.SADNESS, 8));

        thenRecommends(result);
        thenVadIs(result, -0.85, 0.35, -0.75);
        thenUserGoalIs(result, "procesar duelo o tristeza y sentirse acompanado");
    }

    @Test
    @DisplayName("Construye el respaldo de estrés desde la conversación")
    void resolveShouldBuildStressFallbackFromConversation() {
        EmotionalAnalysisResult result = resolveWithReply(null, reply(EmotionType.OVERWHELM, 9));

        thenRecommends(result);
        thenVadIs(result, -0.65, 0.75, -0.65);
        thenUserGoalIs(result, "regular estres y recuperar control");
    }

    @Test
    @DisplayName("Construye el respaldo depresivo desde la conversación")
    void resolveShouldBuildDepressiveFallbackFromConversation() {
        EmotionalAnalysisResult result = resolveWithReply(null, reply(EmotionType.LONELINESS, 8));

        thenRecommends(result);
        thenVadIs(result, -0.90, 0.25, -0.80);
        thenUserGoalIs(result, "sentirse acompanado y aliviar tristeza profunda");
    }

    @Test
    @DisplayName("Construye el respaldo por defecto desde la conversación")
    void resolveShouldBuildDefaultFallbackFromConversation() {
        EmotionalAnalysisResult result = resolveWithReply(null, reply(EmotionType.NUMBNESS, 8));

        thenRecommends(result);
        thenVadIs(result, -0.60, 0.50, -0.60);
        thenUserGoalIs(result, "regular el estado emocional");
    }

    @Test
    @DisplayName("Fuerza una recomendación neutral ante un pedido explícito")
    void resolveShouldForceNeutralRecommendationForExplicitRequest() {
        EmotionalAnalysisResult result = resolveWithForce(neutralAnalysis());

        thenRecommends(result);
        thenEmotionIs(result, EmotionType.NEUTRAL);
        thenUserGoalIs(result, "recibir una actividad de bienestar");
    }

    @Test
    @DisplayName("Ante un pedido explícito con emoción null usa NEUTRAL")
    void resolveShouldForceExplicitRequestWithNullEmotionUsingNeutral() {
        EmotionalAnalysisResult result = resolveWithForce(analysis(false, null, 0.0, 0.0, 0.0, 0.0));

        thenRecommends(result);
        thenEmotionIs(result, EmotionType.NEUTRAL);
        thenUserGoalIs(result, "recibir una actividad de bienestar");
    }

    @Test
    @DisplayName("No aplica override explícito cuando el análisis ya recomienda")
    void resolveShouldNotOverrideWhenForceRequestedButAlreadyRecommends() {
        EmotionalAnalysisResult analysis = analysis(true, EmotionType.ANXIETY, -0.7, 0.8, -0.5, 0.8);

        EmotionalAnalysisResult result = resolveWithForce(analysis);

        thenSameAs(result, analysis);
    }

    // --- arrange ---
    private EmotionalAnalysisResult analysis(
            boolean shouldRecommend,
            EmotionType emotion,
            double valence,
            double arousal,
            double dominance,
            double intensity
    ) {
        return new EmotionalAnalysisResult(
                shouldRecommend, emotion, 0.7, valence, arousal, dominance, intensity, null, null);
    }

    private EmotionalAnalysisResult analysisWithGoal(
            boolean shouldRecommend,
            EmotionType emotion,
            double valence,
            double arousal,
            double dominance,
            double intensity,
            String userGoal,
            String shortReason
    ) {
        return new EmotionalAnalysisResult(
                shouldRecommend, emotion, 0.7, valence, arousal, dominance, intensity, userGoal, shortReason);
    }

    private EmotionalAnalysisResult neutralAnalysis() {
        return EmotionalAnalysisResult.neutral();
    }

    private ChatReply reply(EmotionType emotion, Integer intensity) {
        return new ChatReply("respuesta", emotion, intensity, false, null);
    }

    // --- act ---
    private EmotionalAnalysisResult resolveWithoutForce(EmotionalAnalysisResult analysis) {
        return policy.resolve(USER_ID, analysis, null, false);
    }

    private EmotionalAnalysisResult resolveWithReply(EmotionalAnalysisResult analysis, ChatReply reply) {
        return policy.resolve(USER_ID, analysis, reply, false);
    }

    private EmotionalAnalysisResult resolveWithForce(EmotionalAnalysisResult analysis) {
        return policy.resolve(USER_ID, analysis, null, true);
    }

    // --- assert ---
    private void thenSameAs(EmotionalAnalysisResult result, EmotionalAnalysisResult expected) {
        assertThat(result).isSameAs(expected);
    }

    private void thenRecommends(EmotionalAnalysisResult result) {
        assertThat(result.shouldRecommend()).isTrue();
    }

    private void thenDoesNotRecommend(EmotionalAnalysisResult result) {
        assertThat(result.shouldRecommend()).isFalse();
    }

    private void thenEmotionIs(EmotionalAnalysisResult result, EmotionType emotion) {
        assertThat(result.detectedEmotion()).isEqualTo(emotion);
    }

    private void thenUserGoalIs(EmotionalAnalysisResult result, String goal) {
        assertThat(result.userGoal()).isEqualTo(goal);
    }

    private void thenUserGoalContains(EmotionalAnalysisResult result, String fragment) {
        assertThat(result.userGoal()).contains(fragment);
    }

    private void thenShortReasonIs(EmotionalAnalysisResult result, String reason) {
        assertThat(result.shortReason()).isEqualTo(reason);
    }

    private void thenIntensityIs(EmotionalAnalysisResult result, double intensity) {
        assertThat(result.intensity()).isEqualTo(intensity);
    }

    private void thenVadIs(EmotionalAnalysisResult result, double valence, double arousal, double dominance) {
        assertThat(result.vad().valence()).isEqualTo(valence);
        assertThat(result.vad().arousal()).isEqualTo(arousal);
        assertThat(result.vad().dominance()).isEqualTo(dominance);
    }
}
