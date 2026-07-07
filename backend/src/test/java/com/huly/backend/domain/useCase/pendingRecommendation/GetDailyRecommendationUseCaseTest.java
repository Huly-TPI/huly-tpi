package com.huly.backend.domain.useCase.pendingRecommendation;

import com.huly.backend.domain.dto.pendingRecommendation.GetDailyRecommendationRequest;
import com.huly.backend.domain.dto.pendingRecommendation.PendingRecommendationResponse;
import com.huly.backend.domain.mapper.pendingRecommendation.PendingRecommendationMapper;
import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import com.huly.backend.domain.model.pending.PendingDailyRecommendation;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingRecommendationRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import com.huly.backend.domain.service.pending.TaskBalanceRecommendationResult;
import com.huly.backend.domain.service.pending.TaskBalanceRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDailyRecommendationUseCaseTest {

    @Mock
    private PendingTaskRepository pendingTaskRepository;

    @Mock
    private PendingRecommendationRepository pendingRecommendationRepository;

    @Mock
    private TaskBalanceRecommendationService taskBalanceRecommendationService;

    private GetDailyRecommendationUseCase useCase;

    private static final LocalDate TODAY = LocalDate.of(2026, 1, 10);

    @BeforeEach
    void setUp() {
        useCase = new GetDailyRecommendationUseCase(
                pendingTaskRepository, pendingRecommendationRepository,
                taskBalanceRecommendationService, new PendingRecommendationMapper());
    }

    @Test
    @DisplayName("Devuelve no aplicable cuando el usuario tiene menos de dos tareas pendientes")
    void executeShouldReturnNotApplicableWhenFewerThanTwoPendingTasks() {
        givenPendingTasks(List.of(task(1L, 0.5)));

        PendingRecommendationResponse response = executeUseCase(1L, TODAY);

        thenRecommendationIsApplicable(response, false);
        thenServiceRecommendationNeverCalled();
    }

    @Test
    @DisplayName("Devuelve la recomendación existente en caché cuando el hash del conjunto coincide")
    void executeShouldReturnCachedRecommendationWhenHashMatches() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        String hash = sha256("1:0.2|2:0.5");
        PendingDailyRecommendation existing = recommendation(99L, TODAY, hash, RecommendationResponseDecision.PENDING, List.of(1L));

        givenPendingTasks(pending);
        givenCachedRecommendation(1L, TODAY, existing);

        PendingRecommendationResponse response = executeUseCase(1L, TODAY);

        thenRecommendationIsNew(response, false);
        thenRecommendedTasksContain(response, 1L);
        thenServiceRecommendationNeverCalled();
    }

    @Test
    @DisplayName("Calcula una recomendación nueva cuando no hay ninguna pre-calculada para hoy")
    void executeShouldRecomputeWhenNoExistingRecommendation() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        givenPendingTasks(pending);
        givenCachedRecommendationEmpty(1L, TODAY);
        givenServiceRecommendation(new TaskBalanceRecommendationResult(List.of(1L), 0.2, 1.6));
        givenRecommendationSaved();

        PendingRecommendationResponse response = executeUseCase(1L, TODAY);

        thenRecommendationIsNew(response, true);
        thenRecommendedTasksContain(response, 1L);
    }

    @Test
    @DisplayName("Se recupera con elegancia cuando falla el upsert debido a una carrera concurrente")
    void executeShouldRecoverGracefullyWhenUpsertLosesAConcurrentRecomputeRace() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        PendingDailyRecommendation winnerResult = recommendation(7L, TODAY, sha256("1:0.2|2:0.5"), RecommendationResponseDecision.PENDING, List.of(1L));

        givenPendingTasks(pending);
        givenCachedRecommendationThenWinner(1L, TODAY, winnerResult);
        givenServiceRecommendation(new TaskBalanceRecommendationResult(List.of(1L), 0.2, 1.6));
        givenRecommendationSavingThrows(new ObjectOptimisticLockingFailureException(PendingDailyRecommendation.class, 7L));

        PendingRecommendationResponse response = executeUseCase(1L, TODAY);

        thenRecommendationIsNew(response, false);
        thenRecommendationIdIs(response, 7L);
        thenRecommendedTasksContain(response, 1L);
    }

    @Test
    @DisplayName("No reinicia la decisión a PENDING cuando forceRedecide es false y ya estaba rechazada")
    void executeShouldNotResetDecisionWhenForceRedecideIsFalseAndAlreadyRejected() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        String hash = sha256("1:0.2|2:0.5");
        PendingDailyRecommendation existing = recommendation(99L, TODAY, hash, RecommendationResponseDecision.REJECTED, List.of(1L));

        givenPendingTasks(pending);
        givenCachedRecommendation(1L, TODAY, existing);

        PendingRecommendationResponse response = executeUseCase(1L, TODAY);

        thenDecisionIs(response, RecommendationResponseDecision.REJECTED);
        thenUpdateDecisionNeverCalled();
    }

    @Test
    @DisplayName("Reinicia la decisión a PENDING cuando forceRedecide es true y estaba rechazada")
    void executeShouldResetDecisionToPendingWhenForceRedecideAndPreviouslyRejected() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        String hash = sha256("1:0.2|2:0.5");
        PendingDailyRecommendation existing = recommendation(99L, TODAY, hash, RecommendationResponseDecision.REJECTED, List.of(1L));
        PendingDailyRecommendation reset = recommendation(99L, TODAY, hash, RecommendationResponseDecision.PENDING, List.of(1L));

        givenPendingTasks(pending);
        givenCachedRecommendation(1L, TODAY, existing);
        givenDecisionUpdated(99L, RecommendationResponseDecision.PENDING, reset);

        PendingRecommendationResponse response = executeUseCaseWithForce(1L, TODAY, true);

        thenDecisionIs(response, RecommendationResponseDecision.PENDING);
        thenRecommendedTasksContain(response, 1L);
        thenUpdateDecisionCalled(99L, RecommendationResponseDecision.PENDING);
    }

    @Test
    @DisplayName("Reinicia la decisión a PENDING cuando forceRedecide es true y estaba aceptada")
    void executeShouldResetDecisionToPendingWhenForceRedecideAndPreviouslyAccepted() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        String hash = sha256("1:0.2|2:0.5");
        PendingDailyRecommendation existing = recommendation(99L, TODAY, hash, RecommendationResponseDecision.ACCEPTED, List.of(1L));
        PendingDailyRecommendation reset = recommendation(99L, TODAY, hash, RecommendationResponseDecision.PENDING, List.of(1L));

        givenPendingTasks(pending);
        givenCachedRecommendation(1L, TODAY, existing);
        givenDecisionUpdated(99L, RecommendationResponseDecision.PENDING, reset);

        PendingRecommendationResponse response = executeUseCaseWithForce(1L, TODAY, true);

        thenDecisionIs(response, RecommendationResponseDecision.PENDING);
        thenUpdateDecisionCalled(99L, RecommendationResponseDecision.PENDING);
    }

    @Test
    @DisplayName("No llama a updateDecision cuando forceRedecide es true pero ya estaba PENDING")
    void executeShouldNotCallUpdateDecisionWhenForceRedecideAndAlreadyPending() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        String hash = sha256("1:0.2|2:0.5");
        PendingDailyRecommendation existing = recommendation(99L, TODAY, hash, RecommendationResponseDecision.PENDING, List.of(1L));

        givenPendingTasks(pending);
        givenCachedRecommendation(1L, TODAY, existing);

        PendingRecommendationResponse response = executeUseCaseWithForce(1L, TODAY, true);

        thenDecisionIs(response, RecommendationResponseDecision.PENDING);
        thenUpdateDecisionNeverCalled();
    }

    @Test
    @DisplayName("Vuelve a calcular normalmente cuando forceRedecide es true pero el hash del conjunto cambió")
    void executeShouldRecomputeNormallyWhenForceRedecideAndHashChanged() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        PendingDailyRecommendation staleExisting = recommendation(99L, TODAY, "stale-hash", RecommendationResponseDecision.REJECTED, List.of(1L));

        givenPendingTasks(pending);
        givenCachedRecommendation(1L, TODAY, staleExisting);
        givenServiceRecommendation(new TaskBalanceRecommendationResult(List.of(2L), 0.5, 1.6));
        givenRecommendationSaved();

        PendingRecommendationResponse response = executeUseCaseWithForce(1L, TODAY, true);

        thenRecommendationIsNew(response, true);
        thenDecisionIs(response, RecommendationResponseDecision.PENDING);
        thenRecommendedTasksContain(response, 2L);
        thenUpdateDecisionNeverCalled();
    }

    // --- arrange ---

    private void givenPendingTasks(List<PendingTask> tasks) {
        when(pendingTaskRepository.findPendingByUserId(1L)).thenReturn(tasks);
    }

    private void givenCachedRecommendation(Long userId, LocalDate date, PendingDailyRecommendation rec) {
        when(pendingRecommendationRepository.findByUserIdAndDate(userId, date)).thenReturn(Optional.of(rec));
    }

    private void givenCachedRecommendationEmpty(Long userId, LocalDate date) {
        when(pendingRecommendationRepository.findByUserIdAndDate(userId, date)).thenReturn(Optional.empty());
    }

    private void givenCachedRecommendationThenWinner(Long userId, LocalDate date, PendingDailyRecommendation winner) {
        when(pendingRecommendationRepository.findByUserIdAndDate(userId, date))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(winner));
    }

    private void givenServiceRecommendation(TaskBalanceRecommendationResult result) {
        when(taskBalanceRecommendationService.recommend(any(), anyDouble(), any())).thenReturn(result);
    }

    private void givenRecommendationSaved() {
        when(pendingRecommendationRepository.upsert(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void givenRecommendationSavingThrows(Throwable t) {
        when(pendingRecommendationRepository.upsert(any())).thenThrow(t);
    }

    private void givenDecisionUpdated(Long id, RecommendationResponseDecision decision, PendingDailyRecommendation result) {
        when(pendingRecommendationRepository.updateDecision(id, decision, null)).thenReturn(result);
    }

    // --- act ---

    private PendingRecommendationResponse executeUseCase(Long userId, LocalDate date) {
        return useCase.execute(new GetDailyRecommendationRequest(userId, date));
    }

    private PendingRecommendationResponse executeUseCaseWithForce(Long userId, LocalDate date, boolean force) {
        return useCase.execute(new GetDailyRecommendationRequest(userId, date, force));
    }

    // --- assert ---

    private void thenRecommendationIsApplicable(PendingRecommendationResponse response, boolean expected) {
        assertThat(response.applicable()).isEqualTo(expected);
    }

    private void thenServiceRecommendationNeverCalled() {
        verify(taskBalanceRecommendationService, never()).recommend(any(), anyDouble(), any());
    }

    private void thenRecommendationIsNew(PendingRecommendationResponse response, boolean expected) {
        assertThat(response.isNew()).isEqualTo(expected);
    }

    private void thenRecommendedTasksContain(PendingRecommendationResponse response, Long... taskIds) {
        assertThat(response.recommendedTaskIds()).containsExactly(taskIds);
    }

    private void thenRecommendationIdIs(PendingRecommendationResponse response, Long expectedId) {
        assertThat(response.recommendationId()).isEqualTo(expectedId);
    }

    private void thenDecisionIs(PendingRecommendationResponse response, RecommendationResponseDecision expected) {
        assertThat(response.decision()).isEqualTo(expected);
    }

    private void thenUpdateDecisionNeverCalled() {
        verify(pendingRecommendationRepository, never()).updateDecision(any(), any(), any());
    }

    private void thenUpdateDecisionCalled(Long id, RecommendationResponseDecision decision) {
        verify(pendingRecommendationRepository).updateDecision(id, decision, null);
    }

    // --- helpers ---

    private PendingTask task(Long id, double score) {
        return PendingTask.builder()
                .id(id).userId(1L).title("Tarea " + id).status(PendingStatus.PENDING)
                .mentalLoadScore(score).mentalLoadBucket(MentalLoadBucket.MEDIUM).build();
    }

    private PendingDailyRecommendation recommendation(Long id, LocalDate date, String hash, RecommendationResponseDecision decision, List<Long> taskIds) {
        return PendingDailyRecommendation.builder()
                .id(id).userId(1L).recommendationDate(date)
                .decision(decision)
                .pendingSetHash(hash)
                .recommendedTaskIds(taskIds)
                .build();
    }

    private String sha256(String value) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
