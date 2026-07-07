package com.huly.backend.domain.useCase.cloudRecommendation;

import com.huly.backend.domain.dto.cloudRecommendation.GetCloudRecommendationRequest;
import com.huly.backend.domain.dto.cloudRecommendation.GetCloudRecommendationResponse;
import com.huly.backend.domain.mapper.cloudRecommendation.GetCloudRecommendationMapper;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationResult;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.port.EmotionalAnalysisPort;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationPolicy;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.service.emotionalRecommendation.EmotionalRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCloudRecommendationUseCaseTest {

    private static final Long USER_ID = 7L;
    private static final int HISTORY_LIMIT = 20;
    private static final List<String> THOUGHTS = List.of("no puedo dormir", "estoy muy cansado");
    private static final String ITEM_TITLE = "Actividad sugerida";
    private static final String ITEM_DESCRIPTION = "Probá esta actividad";
    private static final String FALLBACK_TITLE = "Escribí en tu diario";
    private static final String FALLBACK_DESCRIPTION =
            "Plasmar lo que sentiste puede ayudarte a procesarlo con más profundidad.";

    @Mock
    private Resource cloudAnalysisPrompt;
    @Mock
    private EmotionalAnalysisPort emotionalAnalysisPort;
    @Mock
    private PromptBuilderService promptBuilderService;
    @Mock
    private ChatEmotionalRecommendationPolicy recommendationPolicy;
    @Mock
    private EmotionalRecommendationService recommendationService;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private EmotionalEventRepository emotionalEventRepository;

    private GetCloudRecommendationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = buildUseCase(cloudAnalysisPrompt);
    }

    @Test
    @DisplayName("Recomienda respiración cuando la mejor actividad es BREATHING")
    void executeShouldRecommendBreathingWhenTopActivityIsBreathing() {
        givenPromptTemplate();
        givenBuiltPrompt();
        givenAnalysis();
        givenResolvedAnalysis();
        givenActivities();
        givenUserHistory();
        givenTopRecommendation(ActivityType.BREATHING);

        GetCloudRecommendationResponse result = recommend();

        thenRecommendationIs(result, "breathing", "/guided-breathing");
    }

    @Test
    @DisplayName("Recomienda farolitos cuando la mejor actividad es LANTERN")
    void executeShouldRecommendLanternsWhenTopActivityIsLantern() {
        givenPromptTemplate();
        givenBuiltPrompt();
        givenAnalysis();
        givenResolvedAnalysis();
        givenActivities();
        givenUserHistory();
        givenTopRecommendation(ActivityType.LANTERN);

        GetCloudRecommendationResponse result = recommend();

        thenRecommendationIs(result, "lanterns", "/lanterns");
    }

    @Test
    @DisplayName("Recomienda burbujas cuando la mejor actividad es BUBBLE")
    void executeShouldRecommendBubblesWhenTopActivityIsBubble() {
        givenPromptTemplate();
        givenBuiltPrompt();
        givenAnalysis();
        givenResolvedAnalysis();
        givenActivities();
        givenUserHistory();
        givenTopRecommendation(ActivityType.BUBBLE);

        GetCloudRecommendationResponse result = recommend();

        thenRecommendationIs(result, "bubbles", "/bubbles");
    }

    @Test
    @DisplayName("Recomienda el diario para cualquier otra actividad (DIARY)")
    void executeShouldRecommendDiaryWhenTopActivityIsDiary() {
        givenPromptTemplate();
        givenBuiltPrompt();
        givenAnalysis();
        givenResolvedAnalysis();
        givenActivities();
        givenUserHistory();
        givenTopRecommendation(ActivityType.DIARY);

        GetCloudRecommendationResponse result = recommend();

        thenRecommendationIs(result, "diary", "/diary");
    }

    @Test
    @DisplayName("Usa el fallback cuando el ranking no devuelve recomendaciones")
    void executeShouldUseFallbackWhenNoRecommendations() {
        givenPromptTemplate();
        givenBuiltPrompt();
        givenAnalysis();
        givenResolvedAnalysis();
        givenActivities();
        givenUserHistory();
        givenNoRecommendations();

        GetCloudRecommendationResponse result = recommend();

        thenFallbackReturned(result);
    }

    @Test
    @DisplayName("Usa el fallback cuando falla el proceso de recomendación")
    void executeShouldUseFallbackWhenRecommendationFails() {
        givenPromptTemplate();
        givenBuiltPrompt();
        givenAnalysis();
        givenResolvedAnalysis();
        givenActivities();
        givenUserHistory();
        givenRecommendationFails();

        GetCloudRecommendationResponse result = recommend();

        thenFallbackReturned(result);
    }

    @Test
    @DisplayName("Usa un análisis neutral cuando el puerto de análisis devuelve null")
    void executeShouldUseNeutralAnalysisWhenPortReturnsNull() {
        givenPromptTemplate();
        givenBuiltPrompt();
        givenNullAnalysis();
        givenResolvedAnalysis();
        givenActivities();
        givenUserHistory();
        givenTopRecommendation(ActivityType.DIARY);

        GetCloudRecommendationResponse result = recommend();

        thenRecommendationIs(result, "diary", "/diary");
    }

    @Test
    @DisplayName("No consulta el historial cuando el usuario es anónimo (userId null)")
    void executeShouldReadEmptyHistoryWhenUserIdIsNull() {
        givenPromptTemplate();
        givenBuiltPrompt();
        givenAnalysis();
        givenResolvedAnalysis();
        givenActivities();
        givenTopRecommendation(ActivityType.BREATHING);

        GetCloudRecommendationResponse result = recommendForAnonymous();

        thenRecommendationIs(result, "breathing", "/guided-breathing");
        thenUserHistoryNotFetched();
    }

    @Test
    @DisplayName("Continúa con prompt vacío cuando falla la lectura del recurso de prompt")
    void executeShouldContinueWhenPromptResourceReadFails() {
        givenPromptReadFails();
        givenBuiltPrompt();
        givenAnalysis();
        givenResolvedAnalysis();
        givenActivities();
        givenUserHistory();
        givenTopRecommendation(ActivityType.DIARY);

        GetCloudRecommendationResponse result = recommend();

        thenRecommendationIs(result, "diary", "/diary");
    }

    @Test
    @DisplayName("Usa prompt vacío cuando no hay recurso de prompt configurado")
    void executeShouldUseEmptyPromptWhenResourceIsNull() {
        givenNoPromptResource();
        givenBuiltPrompt();
        givenAnalysis();
        givenResolvedAnalysis();
        givenActivities();
        givenUserHistory();
        givenTopRecommendation(ActivityType.DIARY);

        GetCloudRecommendationResponse result = recommend();

        thenRecommendationIs(result, "diary", "/diary");
    }

    // --- arrange ---

    private GetCloudRecommendationUseCase buildUseCase(Resource prompt) {
        return new GetCloudRecommendationUseCase(
                prompt,
                emotionalAnalysisPort,
                promptBuilderService,
                recommendationPolicy,
                recommendationService,
                activityRepository,
                emotionalEventRepository,
                new GetCloudRecommendationMapper());
    }

    private void givenPromptTemplate() {
        try {
            when(cloudAnalysisPrompt.getContentAsString(StandardCharsets.UTF_8)).thenReturn("plantilla");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void givenPromptReadFails() {
        try {
            when(cloudAnalysisPrompt.getContentAsString(StandardCharsets.UTF_8))
                    .thenThrow(new IOException("no se pudo leer el prompt"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void givenNoPromptResource() {
        useCase = buildUseCase(null);
    }

    private void givenBuiltPrompt() {
        when(promptBuilderService.buildEmotionalAnalysisPrompt(any(), any())).thenReturn("prompt");
    }

    private void givenAnalysis() {
        when(emotionalAnalysisPort.analyze(any(), any(), any())).thenReturn(EmotionalAnalysisResult.neutral());
    }

    private void givenNullAnalysis() {
        when(emotionalAnalysisPort.analyze(any(), any(), any())).thenReturn(null);
    }

    private void givenResolvedAnalysis() {
        when(recommendationPolicy.resolve(any(), any(), any(), anyBoolean()))
                .thenReturn(EmotionalAnalysisResult.neutral());
    }

    private void givenActivities() {
        when(activityRepository.findAll()).thenReturn(List.of());
    }

    private void givenUserHistory() {
        when(emotionalEventRepository.findRecentRecommendationHistoryByUserId(USER_ID, HISTORY_LIMIT))
                .thenReturn(List.of());
    }

    private void givenTopRecommendation(ActivityType type) {
        when(recommendationService.recommend(any(), any(), any()))
                .thenReturn(new EmotionalRecommendationResult(List.of(item(type)), false));
    }

    private void givenNoRecommendations() {
        when(recommendationService.recommend(any(), any(), any()))
                .thenReturn(new EmotionalRecommendationResult(List.of(), false));
    }

    private void givenRecommendationFails() {
        when(recommendationService.recommend(any(), any(), any()))
                .thenThrow(new RuntimeException("fallo de ranking"));
    }

    private EmotionalRecommendationItem item(ActivityType type) {
        return new EmotionalRecommendationItem(1L, type, ITEM_TITLE, ITEM_DESCRIPTION, 0.9, "razón", "/ruta");
    }

    // --- act ---

    private GetCloudRecommendationResponse recommend() {
        return useCase.execute(new GetCloudRecommendationRequest(THOUGHTS, USER_ID));
    }

    private GetCloudRecommendationResponse recommendForAnonymous() {
        return useCase.execute(new GetCloudRecommendationRequest(THOUGHTS, null));
    }

    // --- assert ---

    private void thenRecommendationIs(GetCloudRecommendationResponse result, String activityType, String redirectUrl) {
        assertThat(result.activityType()).isEqualTo(activityType);
        assertThat(result.actionId()).isEqualTo(activityType);
        assertThat(result.title()).isEqualTo(ITEM_TITLE);
        assertThat(result.description()).isEqualTo(ITEM_DESCRIPTION);
        assertThat(result.redirectUrl()).isEqualTo(redirectUrl);
    }

    private void thenFallbackReturned(GetCloudRecommendationResponse result) {
        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.actionId()).isEqualTo("diary");
        assertThat(result.title()).isEqualTo(FALLBACK_TITLE);
        assertThat(result.description()).isEqualTo(FALLBACK_DESCRIPTION);
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    private void thenUserHistoryNotFetched() {
        verify(emotionalEventRepository, never()).findRecentRecommendationHistoryByUserId(anyLong(), anyInt());
    }
}
