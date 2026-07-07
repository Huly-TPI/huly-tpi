package com.huly.backend.infrastructure.presentation.dto.emotionalEvent;

import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EmotionalEventResponseTest {

    private static final Long ID = 1L;
    private static final Long USER_ID = 42L;
    private static final EmotionalEventSource SOURCE = EmotionalEventSource.CHATBOT;
    private static final String INPUT_TEXT = "Hoy me siento abrumado por el trabajo";
    private static final String DETECTED_EMOTION = "ansiedad";
    private static final Double CONFIDENCE = 0.87;
    private static final Double VALENCE = -0.4;
    private static final Double AROUSAL = 0.6;
    private static final Double DOMINANCE = 0.3;
    private static final Double INTENSITY = 0.75;
    private static final String USER_GOAL = "reducir el estrés";
    private static final String GENERATED_RECOMMENDATION = "Probá una respiración guiada de 5 minutos";
    private static final Long RECOMMENDED_ACTIVITY_ID = 100L;
    private static final Long CHOSEN_ACTIVITY_ID = 200L;
    private static final RecommendationDecision RECOMMENDATION_DECISION = RecommendationDecision.CHOSE_OTHER;
    private static final Integer FEEDBACK_SCORE = 4;
    private static final String FEEDBACK_TEXT = "Me ayudó bastante";
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:15:30Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-01-02T11:20:40Z");

    private EmotionalEvent event;

    @Test
    @DisplayName("Mapea todos los campos del evento emocional al response")
    void fromShouldMapAllFields() {
        // --- arrange ---
        givenFullyPopulatedEvent();

        // --- act ---
        EmotionalEventResponse result = mapFromEvent();

        // --- assert ---
        thenAllFieldsAreMapped(result);
    }

    // --- arrange ---

    private void givenFullyPopulatedEvent() {
        event = EmotionalEvent.builder()
                .id(ID)
                .userId(USER_ID)
                .source(SOURCE)
                .inputText(INPUT_TEXT)
                .detectedEmotion(DETECTED_EMOTION)
                .confidence(CONFIDENCE)
                .valence(VALENCE)
                .arousal(AROUSAL)
                .dominance(DOMINANCE)
                .intensity(INTENSITY)
                .userGoal(USER_GOAL)
                .generatedRecommendation(GENERATED_RECOMMENDATION)
                .recommendedActivityId(RECOMMENDED_ACTIVITY_ID)
                .chosenActivityId(CHOSEN_ACTIVITY_ID)
                .recommendationDecision(RECOMMENDATION_DECISION)
                .feedbackScore(FEEDBACK_SCORE)
                .feedbackText(FEEDBACK_TEXT)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }

    // --- act ---

    private EmotionalEventResponse mapFromEvent() {
        return EmotionalEventResponse.from(event);
    }

    // --- assert ---

    /** Verifica que cada campo del dominio se copió sin transformación al response. */
    private void thenAllFieldsAreMapped(EmotionalEventResponse result) {
        assertThat(result.id()).isEqualTo(ID);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.source()).isEqualTo(SOURCE);
        assertThat(result.inputText()).isEqualTo(INPUT_TEXT);
        assertThat(result.detectedEmotion()).isEqualTo(DETECTED_EMOTION);
        assertThat(result.confidence()).isEqualTo(CONFIDENCE);
        assertThat(result.valence()).isEqualTo(VALENCE);
        assertThat(result.arousal()).isEqualTo(AROUSAL);
        assertThat(result.dominance()).isEqualTo(DOMINANCE);
        assertThat(result.intensity()).isEqualTo(INTENSITY);
        assertThat(result.userGoal()).isEqualTo(USER_GOAL);
        assertThat(result.generatedRecommendation()).isEqualTo(GENERATED_RECOMMENDATION);
        assertThat(result.recommendedActivityId()).isEqualTo(RECOMMENDED_ACTIVITY_ID);
        assertThat(result.chosenActivityId()).isEqualTo(CHOSEN_ACTIVITY_ID);
        assertThat(result.recommendationDecision()).isEqualTo(RECOMMENDATION_DECISION);
        assertThat(result.feedbackScore()).isEqualTo(FEEDBACK_SCORE);
        assertThat(result.feedbackText()).isEqualTo(FEEDBACK_TEXT);
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
        assertThat(result.updatedAt()).isEqualTo(UPDATED_AT);
    }
}
