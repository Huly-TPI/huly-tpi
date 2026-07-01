package com.huly.backend.domain.mapper.chat;

import com.huly.backend.domain.dto.emotionalEvent.CreateEmotionalEventRequest;
import com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.domain.dto.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMapperTest {

    private final ChatMapper mapper = new ChatMapper();

    @Test
    @DisplayName("Arma el request de evento emocional del chatbot con los datos del análisis")
    void toCreateEmotionalEventRequestShouldMapAnalysisAndRecommendation() {
        EmotionalAnalysisResult analysis = analysis(EmotionType.GRIEF, 0.92, 0.88, "acompanarse");
        EmotionalRecommendationItem recommendation = recommendation(
                7L, "Diario emocional", "Un espacio para ordenar", "Recomendada");

        CreateEmotionalEventRequest request = mapper.toCreateEmotionalEventRequest(
                "me siento mal", 3L, analysis, recommendation);

        assertThat(request.source()).isEqualTo(EmotionalEventSource.CHATBOT);
        assertThat(request.inputText()).isEqualTo("me siento mal");
        assertThat(request.detectedEmotion()).isEqualTo("GRIEF");
        assertThat(request.confidence()).isEqualTo(0.92);
        assertThat(request.intensity()).isEqualTo(0.88);
        assertThat(request.userGoal()).isEqualTo("acompanarse");
        assertThat(request.recommendedActivityId()).isEqualTo(7L);
        assertThat(request.chosenActivityId()).isNull();
        assertThat(request.generatedRecommendation())
                .isEqualTo("Diario emocional: Un espacio para ordenar Recomendada");
    }

    @Test
    @DisplayName("Usa NEUTRAL como emoción por defecto cuando el análisis no detecta ninguna")
    void toCreateEmotionalEventRequestShouldDefaultToNeutralEmotion() {
        EmotionalAnalysisResult analysis = analysis(null, 0.5, 0.2, null);
        EmotionalRecommendationItem recommendation = recommendation(
                1L, "Respiración", "Práctica breve", "Ayuda a regular");

        CreateEmotionalEventRequest request = mapper.toCreateEmotionalEventRequest(
                "hola", 1L, analysis, recommendation);

        assertThat(request.detectedEmotion()).isEqualTo("NEUTRAL");
    }

    @Test
    @DisplayName("Omite la razón cuando viene vacía al armar el texto de la recomendación")
    void toCreateEmotionalEventRequestShouldOmitBlankReason() {
        EmotionalAnalysisResult analysis = analysis(EmotionType.STRESS, 0.9, 0.7, "calmarse");
        EmotionalRecommendationItem recommendation = recommendation(
                2L, "Respiración", "Práctica breve", null);

        CreateEmotionalEventRequest request = mapper.toCreateEmotionalEventRequest(
                "estresado", 1L, analysis, recommendation);

        assertThat(request.generatedRecommendation()).isEqualTo("Respiración: Práctica breve");
    }

    @Test
    @DisplayName("Arma la acción sugerida con la URL de actividades y el id del evento")
    void toSuggestedActionShouldMapRecommendationAndEvent() {
        EmotionalRecommendationItem recommendation = recommendation(
                7L, "Diario emocional", "Un espacio para ordenar", "Recomendada");
        EmotionalEventResponse event = eventResponse(50L);

        SuggestedChatAction action = mapper.toSuggestedAction(recommendation, event);

        assertThat(action.type()).isEqualTo(ActivityType.DIARY);
        assertThat(action.activityId()).isEqualTo(7L);
        assertThat(action.title()).isEqualTo("Diario emocional");
        assertThat(action.description()).isEqualTo("Un espacio para ordenar");
        assertThat(action.actionUrl()).isEqualTo("/api/activities");
        assertThat(action.emotionalEventId()).isEqualTo(50L);
    }

    private EmotionalAnalysisResult analysis(
            EmotionType emotion,
            double confidence,
            double intensity,
            String userGoal) {
        return new EmotionalAnalysisResult(
                true, emotion, confidence, -0.5, 0.5, -0.5, intensity, userGoal, "motivo");
    }

    private EmotionalRecommendationItem recommendation(
            Long activityId,
            String title,
            String description,
            String reason) {
        return new EmotionalRecommendationItem(
                activityId, ActivityType.DIARY, title, description, 0.9, reason);
    }

    private EmotionalEventResponse eventResponse(Long id) {
        Instant now = Instant.now();
        return new EmotionalEventResponse(
                id,
                1L,
                EmotionalEventSource.CHATBOT,
                null,
                "GRIEF",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                7L,
                null,
                (RecommendationDecision) null,
                null,
                null,
                now,
                now
        );
    }
}
