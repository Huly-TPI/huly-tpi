package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.model.CreateEmotionalEventCommand;
import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.UpdateEmotionalEventFeedbackCommand;
import com.huly.backend.domain.model.UpdateRecommendationDecisionCommand;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.repository.ActivityRepository;
import com.huly.backend.domain.repository.EmotionalEventRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmotionalEventUseCaseTest {

    private EmotionalEventRepository emotionalEventRepository;
    private ActivityRepository activityRepository;
    private UserVectorMemoryService userVectorMemoryService;
    private CreateEmotionalEventUseCase createUseCase;
    private UpdateEmotionalEventDecisionUseCase decisionUseCase;
    private UpdateEmotionalEventFeedbackUseCase feedbackUseCase;

    @BeforeEach
    void setUp() {
        emotionalEventRepository = mock(EmotionalEventRepository.class);
        activityRepository = mock(ActivityRepository.class);
        userVectorMemoryService = mock(UserVectorMemoryService.class);
        createUseCase = new CreateEmotionalEventUseCase(emotionalEventRepository, activityRepository);
        decisionUseCase = new UpdateEmotionalEventDecisionUseCase(
                emotionalEventRepository,
                activityRepository,
                userVectorMemoryService);
        feedbackUseCase = new UpdateEmotionalEventFeedbackUseCase(emotionalEventRepository);
    }

    @Test
    void create_shouldSaveValidEmotionalEvent() {
        when(activityRepository.existsById(1L)).thenReturn(true);
        when(emotionalEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmotionalEvent result = createUseCase.execute(validCreateCommand());

        assertThat(result.getSource()).isEqualTo(EmotionalEventSource.CHATBOT);
        assertThat(result.getDetectedEmotion()).isEqualTo("ANSIEDAD");
        assertThat(result.getRecommendedActivityId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isNotNull();
        verify(emotionalEventRepository).save(any(EmotionalEvent.class));
    }

    @Test
    void create_shouldValidateVadConfidenceAndIntensityRanges() {
        CreateEmotionalEventCommand invalid = new CreateEmotionalEventCommand(
                1L, EmotionalEventSource.CHATBOT, "texto", "ANSIEDAD",
                1.2, -0.8, 0.9, -0.7, 0.85,
                "calmarme", "Respira", null, null
        );

        assertThatThrownBy(() -> createUseCase.execute(invalid))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("confidence");
    }

    @Test
    void updateDecision_shouldSaveAcceptedUsingRecommendedActivityWhenChosenIsMissing() {
        EmotionalEvent event = persistedEvent().toBuilder()
                .recommendedActivityId(1L)
                .build();
        when(emotionalEventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(activityRepository.existsById(1L)).thenReturn(true);
        when(emotionalEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmotionalEvent result = decisionUseCase.execute(
                10L,
                new UpdateRecommendationDecisionCommand(RecommendationDecision.ACCEPTED, null)
        );

        assertThat(result.getRecommendationDecision()).isEqualTo(RecommendationDecision.ACCEPTED);
        assertThat(result.getChosenActivityId()).isEqualTo(1L);
    }

    @Test
    void updateDecision_shouldSaveIgnoredWithoutChosenActivity() {
        when(emotionalEventRepository.findById(10L)).thenReturn(Optional.of(persistedEvent()));
        when(emotionalEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmotionalEvent result = decisionUseCase.execute(
                10L,
                new UpdateRecommendationDecisionCommand(RecommendationDecision.IGNORED, 1L)
        );

        assertThat(result.getRecommendationDecision()).isEqualTo(RecommendationDecision.IGNORED);
        assertThat(result.getChosenActivityId()).isNull();
    }

    @Test
    void updateDecision_shouldSaveChoseOtherWithChosenActivity() {
        when(emotionalEventRepository.findById(10L)).thenReturn(Optional.of(persistedEvent()));
        when(activityRepository.existsById(4L)).thenReturn(true);
        when(emotionalEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmotionalEvent result = decisionUseCase.execute(
                10L,
                new UpdateRecommendationDecisionCommand(RecommendationDecision.CHOSE_OTHER, 4L)
        );

        assertThat(result.getRecommendationDecision()).isEqualTo(RecommendationDecision.CHOSE_OTHER);
        assertThat(result.getChosenActivityId()).isEqualTo(4L);
    }

    @Test
    void updateDecision_shouldRequireChosenActivityForChoseOther() {
        when(emotionalEventRepository.findById(10L)).thenReturn(Optional.of(persistedEvent()));

        assertThatThrownBy(() -> decisionUseCase.execute(
                10L,
                new UpdateRecommendationDecisionCommand(RecommendationDecision.CHOSE_OTHER, null)
        )).isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateFeedback_shouldSaveFeedback() {
        when(emotionalEventRepository.findById(10L)).thenReturn(Optional.of(persistedEvent()));
        when(emotionalEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmotionalEvent result = feedbackUseCase.execute(
                10L,
                new UpdateEmotionalEventFeedbackCommand(4, "Me siento un poco mas tranquilo")
        );

        assertThat(result.getFeedbackScore()).isEqualTo(4);
        assertThat(result.getFeedbackText()).isEqualTo("Me siento un poco mas tranquilo");
    }

    private CreateEmotionalEventCommand validCreateCommand() {
        return new CreateEmotionalEventCommand(
                1L,
                EmotionalEventSource.CHATBOT,
                "Estoy muy ansioso",
                "ANSIEDAD",
                0.91,
                -0.8,
                0.9,
                -0.7,
                0.85,
                "calmarme",
                "Te recomiendo una respiracion guiada",
                1L,
                null
        );
    }

    private EmotionalEvent persistedEvent() {
        Instant now = Instant.now();
        return EmotionalEvent.builder()
                .id(10L)
                .userId(1L)
                .source(EmotionalEventSource.CHATBOT)
                .inputText("Estoy muy ansioso")
                .detectedEmotion("ANSIEDAD")
                .confidence(0.91)
                .valence(-0.8)
                .arousal(0.9)
                .dominance(-0.7)
                .intensity(0.85)
                .userGoal("calmarme")
                .generatedRecommendation("Respira")
                .recommendedActivityId(1L)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
