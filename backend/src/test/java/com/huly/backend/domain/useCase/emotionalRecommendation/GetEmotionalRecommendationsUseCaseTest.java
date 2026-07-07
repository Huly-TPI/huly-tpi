package com.huly.backend.domain.useCase.emotionalRecommendation;

import com.huly.backend.domain.dto.emotionalRecommendation.GetEmotionalRecommendationsRequest;
import com.huly.backend.domain.dto.emotionalRecommendation.GetEmotionalRecommendationsResponse;
import com.huly.backend.domain.mapper.emotionalRecommendation.GetEmotionalRecommendationsMapper;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendation;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationResult;
import com.huly.backend.domain.model.emotionalRecommendation.Vad;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.service.emotionalRecommendation.EmotionalRecommendationService;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetEmotionalRecommendationsUseCaseTest {

    private static final Long USER_ID = 7L;
    private static final String USER_GOAL = "calmarme";
    private static final double VALID_INTENSITY = 0.8;
    private static final int HISTORY_LIMIT = 20;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private EmotionalEventRepository emotionalEventRepository;

    @Mock
    private EmotionalRecommendationService recommendationService;

    private GetEmotionalRecommendationsUseCase useCase;

    private GetEmotionalRecommendationsRequest request;
    private EmotionalRecommendation query;
    private List<Activity> activities;
    private List<EmotionalEvent> userHistory;

    @BeforeEach
    void setUp() {
        useCase = new GetEmotionalRecommendationsUseCase(
                activityRepository, emotionalEventRepository, recommendationService,
                new GetEmotionalRecommendationsMapper());
    }

    @Test
    @DisplayName("Recomienda sin cargar historial cuando el pedido no trae usuario")
    void executeShouldRecommendWithoutHistoryWhenUserIdIsMissing() {
        givenRequest(null, validVad(), VALID_INTENSITY);
        givenActivitiesAvailable();
        givenNoUserHistory();
        givenRecommendationReturns(singleFullItemResult());

        GetEmotionalRecommendationsResponse response = execute();

        thenUserHistoryWasNotLoaded();
        thenDelegatedWithGatheredContext();
        thenResponseHasFullSingleItem(response);
    }

    @Test
    @DisplayName("Carga el historial reciente y delega el ranking cuando el pedido trae usuario")
    void executeShouldRecommendWithUserHistoryWhenUserIdIsPresent() {
        givenRequest(USER_ID, validVad(), VALID_INTENSITY);
        givenActivitiesAvailable();
        givenStoredUserHistory();
        givenRecommendationReturns(emptyResult());

        GetEmotionalRecommendationsResponse response = execute();

        thenUserHistoryWasLoaded();
        thenDelegatedWithGatheredContext();
        thenResponseIsEmptyWithoutFallback(response);
    }

    @Test
    @DisplayName("Propaga el fallback y mapea todas las recomendaciones cuando el servicio usa el fallback")
    void executeShouldReportFallbackWhenServiceFallsBack() {
        givenRequest(null, validVad(), VALID_INTENSITY);
        givenActivitiesAvailable();
        givenNoUserHistory();
        givenRecommendationReturns(fallbackMultiItemResult());

        GetEmotionalRecommendationsResponse response = execute();

        thenResponseReportsFallbackWithRankedItems(response);
    }

    @Test
    @DisplayName("Rechaza y no carga actividades cuando la valencia esta fuera de rango")
    void executeShouldRejectInvalidValence() {
        givenRequest(null, new Vad(-1.1, 0.0, 0.0), VALID_INTENSITY);

        thenExecuteRejectsWithBadRequest("valence");
    }

    @Test
    @DisplayName("Rechaza y no carga actividades cuando el arousal esta fuera de rango")
    void executeShouldRejectInvalidArousal() {
        givenRequest(null, new Vad(0.0, 1.1, 0.0), VALID_INTENSITY);

        thenExecuteRejectsWithBadRequest("arousal");
    }

    @Test
    @DisplayName("Rechaza y no carga actividades cuando la dominancia esta fuera de rango")
    void executeShouldRejectInvalidDominance() {
        givenRequest(null, new Vad(0.0, 0.0, -1.5), VALID_INTENSITY);

        thenExecuteRejectsWithBadRequest("dominance");
    }

    @Test
    @DisplayName("Rechaza y no carga actividades cuando la intensidad supera el maximo")
    void executeShouldRejectIntensityAboveMaximum() {
        givenRequest(null, validVad(), 1.1);

        thenExecuteRejectsWithBadRequest("intensity");
    }

    @Test
    @DisplayName("Rechaza y no carga actividades cuando la intensidad es menor al minimo")
    void executeShouldRejectIntensityBelowMinimum() {
        givenRequest(null, validVad(), -0.1);

        thenExecuteRejectsWithBadRequest("intensity");
    }

    @Test
    @DisplayName("Rechaza y no carga actividades cuando la intensidad no es finita")
    void executeShouldRejectNonFiniteIntensity() {
        givenRequest(null, validVad(), Double.NaN);

        thenExecuteRejectsWithBadRequest("intensity");
    }

    // La rama defensiva validateVad(vad == null) es inalcanzable desde execute():
    // el mapper real (GetEmotionalRecommendationsMapper) siempre construye un Vad no nulo
    // (new Vad(...)), por lo que no puede ejercitarse sin sustituir el mapper por un mock.
    // Toda la logica de ranking/LLM vive en EmotionalRecommendationService (colaborador mockeado),
    // por lo que sus ramas se cubren en el test de ese servicio, no en este use case.

    // --- arrange ---

    private void givenRequest(Long userId, Vad vad, double intensity) {
        request = new GetEmotionalRecommendationsRequest(
                userId, vad.valence(), vad.arousal(), vad.dominance(), intensity, USER_GOAL);
        query = new EmotionalRecommendation(userId, vad, intensity, USER_GOAL);
    }

    private void givenActivitiesAvailable() {
        activities = List.of(Activity.builder().id(1L).build());
        when(activityRepository.findAll()).thenReturn(activities);
    }

    private void givenNoUserHistory() {
        userHistory = List.of();
    }

    private void givenStoredUserHistory() {
        userHistory = List.of(EmotionalEvent.builder().userId(USER_ID).build());
        when(emotionalEventRepository.findRecentRecommendationHistoryByUserId(USER_ID, HISTORY_LIMIT))
                .thenReturn(userHistory);
    }

    private void givenRecommendationReturns(EmotionalRecommendationResult result) {
        when(recommendationService.recommend(query, activities, userHistory)).thenReturn(result);
    }

    private Vad validVad() {
        return new Vad(-0.7, 0.8, -0.6);
    }

    private EmotionalRecommendationResult singleFullItemResult() {
        return new EmotionalRecommendationResult(
                List.of(new EmotionalRecommendationItem(
                        1L, ActivityType.BREATHING, "Respiracion", "Descripcion", 0.9, "razon", "/guided-breathing")),
                false);
    }

    private EmotionalRecommendationResult emptyResult() {
        return new EmotionalRecommendationResult(List.of(), false);
    }

    private EmotionalRecommendationResult fallbackMultiItemResult() {
        return new EmotionalRecommendationResult(
                List.of(
                        new EmotionalRecommendationItem(
                                1L, ActivityType.BREATHING, "Respiracion", "Baja el arousal", 0.9, "r1", "/guided-breathing"),
                        new EmotionalRecommendationItem(
                                2L, ActivityType.DIARY, "Diario", "Ordena ideas", 0.7, "r2", "/diary")),
                true);
    }

    // --- act ---

    private GetEmotionalRecommendationsResponse execute() {
        return useCase.execute(request);
    }

    // --- assert ---

    private void thenUserHistoryWasNotLoaded() {
        verifyNoInteractions(emotionalEventRepository);
    }

    private void thenUserHistoryWasLoaded() {
        verify(emotionalEventRepository).findRecentRecommendationHistoryByUserId(USER_ID, HISTORY_LIMIT);
    }

    private void thenDelegatedWithGatheredContext() {
        verify(recommendationService).recommend(query, activities, userHistory);
    }

    /** Verifica que la respuesta mapea el unico item devolviendo todos sus campos y sin fallback. */
    private void thenResponseHasFullSingleItem(GetEmotionalRecommendationsResponse response) {
        assertThat(response.fallbackUsed()).isFalse();
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).activityId()).isEqualTo(1L);
        assertThat(response.recommendations().get(0).type()).isEqualTo(ActivityType.BREATHING);
        assertThat(response.recommendations().get(0).title()).isEqualTo("Respiracion");
        assertThat(response.recommendations().get(0).description()).isEqualTo("Descripcion");
        assertThat(response.recommendations().get(0).score()).isEqualTo(0.9);
        assertThat(response.recommendations().get(0).reason()).isEqualTo("razon");
        assertThat(response.recommendations().get(0).routePath()).isEqualTo("/guided-breathing");
    }

    private void thenResponseIsEmptyWithoutFallback(GetEmotionalRecommendationsResponse response) {
        assertThat(response.recommendations()).isEmpty();
        assertThat(response.fallbackUsed()).isFalse();
    }

    /** Verifica que el fallback se propaga y que se mapean, en orden, todas las recomendaciones. */
    private void thenResponseReportsFallbackWithRankedItems(GetEmotionalRecommendationsResponse response) {
        assertThat(response.fallbackUsed()).isTrue();
        assertThat(response.recommendations()).hasSize(2);
        assertThat(response.recommendations().get(0).activityId()).isEqualTo(1L);
        assertThat(response.recommendations().get(1).activityId()).isEqualTo(2L);
        assertThat(response.recommendations().get(1).type()).isEqualTo(ActivityType.DIARY);
        assertThat(response.recommendations().get(1).routePath()).isEqualTo("/diary");
    }

    /** Ejercita el rechazo por validacion: lanza BadRequest y no toca los colaboradores de datos/ranking. */
    private void thenExecuteRejectsWithBadRequest(String messageFragment) {
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(messageFragment);
        verify(activityRepository, never()).findAll();
        verifyNoInteractions(recommendationService);
    }
}
