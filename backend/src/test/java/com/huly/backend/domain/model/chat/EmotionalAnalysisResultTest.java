package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.EmotionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmotionalAnalysisResultTest {

    private EmotionalAnalysisResult analysis;

    @Test
    @DisplayName("hasUsableEmotion devuelve true cuando hay emoción y confianza positiva")
    void hasUsableEmotionShouldReturnTrueWhenEmotionAndConfidencePresent() {
        givenAnalysis(EmotionType.JOY, 0.8);

        boolean result = hasUsableEmotion();

        thenHasUsableEmotion(result);
    }

    @Test
    @DisplayName("hasUsableEmotion devuelve false cuando no hay emoción detectada")
    void hasUsableEmotionShouldReturnFalseWhenEmotionIsNull() {
        givenAnalysis(null, 0.8);

        boolean result = hasUsableEmotion();

        thenDoesNotHaveUsableEmotion(result);
    }

    @Test
    @DisplayName("hasUsableEmotion devuelve false cuando la confianza es cero")
    void hasUsableEmotionShouldReturnFalseWhenConfidenceIsZero() {
        givenAnalysis(EmotionType.JOY, 0.0);

        boolean result = hasUsableEmotion();

        thenDoesNotHaveUsableEmotion(result);
    }

    // --- arrange ---

    private void givenAnalysis(EmotionType emotion, double confidence) {
        this.analysis = new EmotionalAnalysisResult(
                true, emotion, confidence, 0.0, 0.0, 0.0, 0.5, null, null);
    }

    // --- act ---

    private boolean hasUsableEmotion() {
        return analysis.hasUsableEmotion();
    }

    // --- assert ---

    private void thenHasUsableEmotion(boolean result) {
        assertThat(result).isTrue();
    }

    private void thenDoesNotHaveUsableEmotion(boolean result) {
        assertThat(result).isFalse();
    }
}
