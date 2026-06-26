package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.dto.emotionalEvent.CreateEmotionalEventRequest;
import com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.domain.dto.emotionalEvent.UpdateEmotionalEventDecisionRequest;
import com.huly.backend.domain.dto.emotionalEvent.UpdateEmotionalEventFeedbackRequest;
import com.huly.backend.domain.mapper.emotionalEvent.CreateEmotionalEventMapper;
import com.huly.backend.domain.mapper.emotionalEvent.UpdateEmotionalEventDecisionMapper;
import com.huly.backend.domain.mapper.emotionalEvent.UpdateEmotionalEventFeedbackMapper;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
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
    private ChatMessageRepository chatMessageRepository;
    private UserVectorMemoryService userVectorMemoryService;
    private CreateEmotionalEventUseCase createUseCase;
    private UpdateEmotionalEventDecisionUseCase decisionUseCase;
    private UpdateEmotionalEventFeedbackUseCase feedbackUseCase;

    @BeforeEach
    void setUp() {
        emotionalEventRepository = mock(EmotionalEventRepository.class);
        activityRepository = mock(ActivityRepository.class);
        chatMessageRepository = mock(ChatMessageRepository.class);
        userVectorMemoryService = mock(UserVectorMemoryService.class);
        createUseCase = new CreateEmotionalEventUseCase(
                emotionalEventRepository, activityRepository, new CreateEmotionalEventMapper());
        decisionUseCase = new UpdateEmotionalEventDecisionUseCase(
                emotionalEventRepository,
                activityRepository,
                userVectorMemoryService,
                chatMessageRepository,
                new UpdateEmotionalEventDecisionMapper());
        feedbackUseCase = new UpdateEmotionalEventFeedbackUseCase(
                emotionalEventRepository, new UpdateEmotionalEventFeedbackMapper());
    }

    @Test
    void create_shouldSaveValidEmotionalEvent() {
        when(activityRepository.existsById(1L)).thenReturn(true);
        when(emotionalEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmotionalEventResponse result = createUseCase.execute(validCreateRequest());

        assertThat(result.source()).isEqualTo(EmotionalEventSource.CHATBOT);
        assertThat(result.detectedEmotion()).isEqualTo("ANSIEDAD");
        assertThat(result.recommendedActivityId()).isEqualTo(1L);
        assertThat(result.createdAt()).isNotNull();
        verify(emotionalEventRepository).save(any(EmotionalEvent.class));
    }

    @Test
    void create_shouldValidateVadConfidenceAndIntensityRanges() {
        CreateEmotionalEventRequest invalid = new CreateEmotionalEventRequest(
                1L, EmotionalEventSource.CHATBOT, "texto", "ANSIEDAD",
                1.2, -0.8, 0.9, -0.7, 0.85,
                "calmarme", "Respira", null, null
        );

        assertThatThrownBy(() -> createUseCase.execute(invalid))
                .isInstanceOf(BusinessRuleException.class)
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

        EmotionalEventResponse result = decisionUseCase.execute(
                new UpdateEmotionalEventDecisionRequest(10L, RecommendationDecision.ACCEPTED, null)
        );

        assertThat(result.recommendationDecision()).isEqualTo(RecommendationDecision.ACCEPTED);
        assertThat(result.chosenActivityId()).isEqualTo(1L);
    }

    @Test
    void updateDecision_shouldSaveIgnoredWithoutChosenActivity() {
        when(emotionalEventRepository.findById(10L)).thenReturn(Optional.of(persistedEvent()));
        when(emotionalEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmotionalEventResponse result = decisionUseCase.execute(
                new UpdateEmotionalEventDecisionRequest(10L, RecommendationDecision.IGNORED, 1L)
        );

        assertThat(result.recommendationDecision()).isEqualTo(RecommendationDecision.IGNORED);
        assertThat(result.chosenActivityId()).isNull();
    }

    @Test
    void updateDecision_shouldSaveChoseOtherWithChosenActivity() {
        when(emotionalEventRepository.findById(10L)).thenReturn(Optional.of(persistedEvent()));
        when(activityRepository.existsById(4L)).thenReturn(true);
        when(emotionalEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmotionalEventResponse result = decisionUseCase.execute(
                new UpdateEmotionalEventDecisionRequest(10L, RecommendationDecision.CHOSE_OTHER, 4L)
        );

        assertThat(result.recommendationDecision()).isEqualTo(RecommendationDecision.CHOSE_OTHER);
        assertThat(result.chosenActivityId()).isEqualTo(4L);
    }

    @Test
    void updateDecision_shouldRequireChosenActivityForChoseOther() {
        when(emotionalEventRepository.findById(10L)).thenReturn(Optional.of(persistedEvent()));

        assertThatThrownBy(() -> decisionUseCase.execute(
                new UpdateEmotionalEventDecisionRequest(10L, RecommendationDecision.CHOSE_OTHER, null)
        )).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updateFeedback_shouldSaveFeedback() {
        when(emotionalEventRepository.findById(10L)).thenReturn(Optional.of(persistedEvent()));
        when(emotionalEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmotionalEventResponse result = feedbackUseCase.execute(
                new UpdateEmotionalEventFeedbackRequest(10L, 4, "Me siento un poco mas tranquilo")
        );

        assertThat(result.feedbackScore()).isEqualTo(4);
        assertThat(result.feedbackText()).isEqualTo("Me siento un poco mas tranquilo");
    }

    private CreateEmotionalEventRequest validCreateRequest() {
        return new CreateEmotionalEventRequest(
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
