package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.EmotionType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatEmotionalRecommendationPolicyTest {

    private final ChatEmotionalRecommendationPolicy policy = new ChatEmotionalRecommendationPolicy();

    @Test
    void resolve_shouldKeepAnalysisThatAlreadyRequestsRecommendation() {
        EmotionalAnalysisResult analysis = analysis(true, EmotionType.ANXIETY, -0.7, 0.8, -0.5, 0.8);

        EmotionalAnalysisResult result = policy.resolve(1L, analysis, null, false);

        assertThat(result).isSameAs(analysis);
    }

    @Test
    void resolve_shouldSuppressRecommendationWhenValenceAndDominanceArePositive() {
        EmotionalAnalysisResult analysis = analysis(true, EmotionType.MOTIVATION, 0.6, 0.4, 0.3, 0.5);

        EmotionalAnalysisResult result = policy.resolve(1L, analysis, null, false);

        assertThat(result.shouldRecommend()).isFalse();
        assertThat(result.detectedEmotion()).isEqualTo(EmotionType.MOTIVATION);
    }

    @Test
    void resolve_shouldSuppressRecommendationForLowIntensityMildNegativeState() {
        EmotionalAnalysisResult analysis = analysis(true, EmotionType.NEUTRAL, -0.3, 0.3, -0.2, 0.5);

        EmotionalAnalysisResult result = policy.resolve(1L, analysis, null, false);

        assertThat(result.shouldRecommend()).isFalse();
    }

    @Test
    void resolve_shouldNotSuppressRecommendationWhenDominanceIsVeryNegative() {
        EmotionalAnalysisResult analysis = analysis(true, EmotionType.FRUSTRATION, -0.45, 0.3, -0.6, 0.65);

        EmotionalAnalysisResult result = policy.resolve(1L, analysis, null, false);

        assertThat(result.shouldRecommend()).isTrue();
    }

    @Test
    void resolve_shouldForceRecommendationForStructuredHighDistress() {
        EmotionalAnalysisResult analysis = analysis(false, EmotionType.SADNESS, -0.8, 0.2, -0.6, 0.4);

        EmotionalAnalysisResult result = policy.resolve(1L, analysis, null, false);

        assertThat(result.shouldRecommend()).isTrue();
        assertThat(result.userGoal()).contains("tristeza");
        assertThat(result.intensity()).isEqualTo(0.65);
    }

    @Test
    void resolve_shouldCreateVadFallbackFromConversationMetadata() {
        EmotionalAnalysisResult neutral = EmotionalAnalysisResult.neutral();
        ChatReply reply = new ChatReply("respuesta", EmotionType.ANXIETY, 8, false, null);

        EmotionalAnalysisResult result = policy.resolve(1L, neutral, reply, false);

        assertThat(result.shouldRecommend()).isTrue();
        assertThat(result.vad().valence()).isEqualTo(-0.75);
        assertThat(result.vad().arousal()).isEqualTo(0.85);
        assertThat(result.vad().dominance()).isEqualTo(-0.70);
    }

    @Test
    void resolve_shouldForceNeutralRecommendationForExplicitRequest() {
        EmotionalAnalysisResult result = policy.resolve(
                1L,
                EmotionalAnalysisResult.neutral(),
                null,
                true
        );

        assertThat(result.shouldRecommend()).isTrue();
        assertThat(result.detectedEmotion()).isEqualTo(EmotionType.NEUTRAL);
        assertThat(result.userGoal()).isEqualTo("recibir una actividad de bienestar");
    }

    private EmotionalAnalysisResult analysis(
            boolean shouldRecommend,
            EmotionType emotion,
            double valence,
            double arousal,
            double dominance,
            double intensity
    ) {
        return new EmotionalAnalysisResult(
                shouldRecommend,
                emotion,
                0.7,
                valence,
                arousal,
                dominance,
                intensity,
                null,
                null
        );
    }
}
