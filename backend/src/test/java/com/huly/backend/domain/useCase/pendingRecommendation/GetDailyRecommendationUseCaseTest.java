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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDate;
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
    void execute_shouldReturnNotApplicable_whenFewerThanTwoPendingTasks() {
        when(pendingTaskRepository.findPendingByUserId(1L)).thenReturn(List.of(task(1L, 0.5)));

        PendingRecommendationResponse response = useCase.execute(new GetDailyRecommendationRequest(1L, TODAY));

        assertThat(response.applicable()).isFalse();
        verify(taskBalanceRecommendationService, never()).recommend(any(), anyDouble(), any());
    }

    @Test
    void execute_shouldReturnCachedRecommendation_whenHashMatches() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        when(pendingTaskRepository.findPendingByUserId(1L)).thenReturn(pending);
        String hash = sha256("1:0.2|2:0.5");
        PendingDailyRecommendation existing = PendingDailyRecommendation.builder()
                .id(99L).userId(1L).recommendationDate(TODAY)
                .decision(RecommendationResponseDecision.PENDING)
                .pendingSetHash(hash)
                .recommendedTaskIds(List.of(1L))
                .build();
        when(pendingRecommendationRepository.findByUserIdAndDate(1L, TODAY)).thenReturn(Optional.of(existing));

        PendingRecommendationResponse response = useCase.execute(new GetDailyRecommendationRequest(1L, TODAY));

        assertThat(response.isNew()).isFalse();
        assertThat(response.recommendedTaskIds()).containsExactly(1L);
        verify(taskBalanceRecommendationService, never()).recommend(any(), anyDouble(), any());
    }

    @Test
    void execute_shouldRecompute_whenNoExistingRecommendation() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        when(pendingTaskRepository.findPendingByUserId(1L)).thenReturn(pending);
        when(pendingRecommendationRepository.findByUserIdAndDate(1L, TODAY)).thenReturn(Optional.empty());
        when(taskBalanceRecommendationService.recommend(any(), anyDouble(), any()))
                .thenReturn(new TaskBalanceRecommendationResult(List.of(1L), 0.2, 1.6));
        when(pendingRecommendationRepository.upsert(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PendingRecommendationResponse response = useCase.execute(new GetDailyRecommendationRequest(1L, TODAY));

        assertThat(response.isNew()).isTrue();
        assertThat(response.recommendedTaskIds()).containsExactly(1L);
    }

    @Test
    void execute_shouldRecoverGracefully_whenUpsertLosesAConcurrentRecomputeRace() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        when(pendingTaskRepository.findPendingByUserId(1L)).thenReturn(pending);
        PendingDailyRecommendation winnerResult = PendingDailyRecommendation.builder()
                .id(7L).userId(1L).recommendationDate(TODAY)
                .decision(RecommendationResponseDecision.PENDING)
                .pendingSetHash(sha256("1:0.2|2:0.5"))
                .recommendedTaskIds(List.of(1L))
                .build();
        when(pendingRecommendationRepository.findByUserIdAndDate(1L, TODAY))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(winnerResult));
        when(taskBalanceRecommendationService.recommend(any(), anyDouble(), any()))
                .thenReturn(new TaskBalanceRecommendationResult(List.of(1L), 0.2, 1.6));
        when(pendingRecommendationRepository.upsert(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(PendingDailyRecommendation.class, 7L));

        PendingRecommendationResponse response = useCase.execute(new GetDailyRecommendationRequest(1L, TODAY));

        assertThat(response.isNew()).isFalse();
        assertThat(response.recommendationId()).isEqualTo(7L);
        assertThat(response.recommendedTaskIds()).containsExactly(1L);
    }

    @Test
    void execute_shouldNotResetDecision_whenForceRedecideIsFalseAndAlreadyRejected() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        when(pendingTaskRepository.findPendingByUserId(1L)).thenReturn(pending);
        String hash = sha256("1:0.2|2:0.5");
        PendingDailyRecommendation existing = PendingDailyRecommendation.builder()
                .id(99L).userId(1L).recommendationDate(TODAY)
                .decision(RecommendationResponseDecision.REJECTED)
                .pendingSetHash(hash)
                .recommendedTaskIds(List.of(1L))
                .build();
        when(pendingRecommendationRepository.findByUserIdAndDate(1L, TODAY)).thenReturn(Optional.of(existing));

        PendingRecommendationResponse response = useCase.execute(new GetDailyRecommendationRequest(1L, TODAY));

        assertThat(response.decision()).isEqualTo(RecommendationResponseDecision.REJECTED);
        verify(pendingRecommendationRepository, never()).updateDecision(any(), any(), any());
    }

    @Test
    void execute_shouldResetDecisionToPending_whenForceRedecideAndPreviouslyRejected() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        when(pendingTaskRepository.findPendingByUserId(1L)).thenReturn(pending);
        String hash = sha256("1:0.2|2:0.5");
        PendingDailyRecommendation existing = PendingDailyRecommendation.builder()
                .id(99L).userId(1L).recommendationDate(TODAY)
                .decision(RecommendationResponseDecision.REJECTED)
                .pendingSetHash(hash)
                .recommendedTaskIds(List.of(1L))
                .build();
        when(pendingRecommendationRepository.findByUserIdAndDate(1L, TODAY)).thenReturn(Optional.of(existing));
        PendingDailyRecommendation reset = PendingDailyRecommendation.builder()
                .id(99L).userId(1L).recommendationDate(TODAY)
                .decision(RecommendationResponseDecision.PENDING)
                .pendingSetHash(hash)
                .recommendedTaskIds(List.of(1L))
                .build();
        when(pendingRecommendationRepository.updateDecision(99L, RecommendationResponseDecision.PENDING, null))
                .thenReturn(reset);

        PendingRecommendationResponse response = useCase.execute(new GetDailyRecommendationRequest(1L, TODAY, true));

        assertThat(response.decision()).isEqualTo(RecommendationResponseDecision.PENDING);
        assertThat(response.recommendedTaskIds()).containsExactly(1L);
        verify(pendingRecommendationRepository).updateDecision(99L, RecommendationResponseDecision.PENDING, null);
    }

    @Test
    void execute_shouldResetDecisionToPending_whenForceRedecideAndPreviouslyAccepted() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        when(pendingTaskRepository.findPendingByUserId(1L)).thenReturn(pending);
        String hash = sha256("1:0.2|2:0.5");
        PendingDailyRecommendation existing = PendingDailyRecommendation.builder()
                .id(99L).userId(1L).recommendationDate(TODAY)
                .decision(RecommendationResponseDecision.ACCEPTED)
                .pendingSetHash(hash)
                .recommendedTaskIds(List.of(1L))
                .build();
        when(pendingRecommendationRepository.findByUserIdAndDate(1L, TODAY)).thenReturn(Optional.of(existing));
        PendingDailyRecommendation reset = PendingDailyRecommendation.builder()
                .id(99L).userId(1L).recommendationDate(TODAY)
                .decision(RecommendationResponseDecision.PENDING)
                .pendingSetHash(hash)
                .recommendedTaskIds(List.of(1L))
                .build();
        when(pendingRecommendationRepository.updateDecision(99L, RecommendationResponseDecision.PENDING, null))
                .thenReturn(reset);

        PendingRecommendationResponse response = useCase.execute(new GetDailyRecommendationRequest(1L, TODAY, true));

        assertThat(response.decision()).isEqualTo(RecommendationResponseDecision.PENDING);
        verify(pendingRecommendationRepository).updateDecision(99L, RecommendationResponseDecision.PENDING, null);
    }

    @Test
    void execute_shouldNotCallUpdateDecision_whenForceRedecideAndAlreadyPending() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        when(pendingTaskRepository.findPendingByUserId(1L)).thenReturn(pending);
        String hash = sha256("1:0.2|2:0.5");
        PendingDailyRecommendation existing = PendingDailyRecommendation.builder()
                .id(99L).userId(1L).recommendationDate(TODAY)
                .decision(RecommendationResponseDecision.PENDING)
                .pendingSetHash(hash)
                .recommendedTaskIds(List.of(1L))
                .build();
        when(pendingRecommendationRepository.findByUserIdAndDate(1L, TODAY)).thenReturn(Optional.of(existing));

        PendingRecommendationResponse response = useCase.execute(new GetDailyRecommendationRequest(1L, TODAY, true));

        assertThat(response.decision()).isEqualTo(RecommendationResponseDecision.PENDING);
        verify(pendingRecommendationRepository, never()).updateDecision(any(), any(), any());
    }

    @Test
    void execute_shouldRecomputeNormally_whenForceRedecideAndHashChanged() {
        List<PendingTask> pending = List.of(task(1L, 0.2), task(2L, 0.5));
        when(pendingTaskRepository.findPendingByUserId(1L)).thenReturn(pending);
        PendingDailyRecommendation staleExisting = PendingDailyRecommendation.builder()
                .id(99L).userId(1L).recommendationDate(TODAY)
                .decision(RecommendationResponseDecision.REJECTED)
                .pendingSetHash("stale-hash")
                .recommendedTaskIds(List.of(1L))
                .build();
        when(pendingRecommendationRepository.findByUserIdAndDate(1L, TODAY)).thenReturn(Optional.of(staleExisting));
        when(taskBalanceRecommendationService.recommend(any(), anyDouble(), any()))
                .thenReturn(new TaskBalanceRecommendationResult(List.of(2L), 0.5, 1.6));
        when(pendingRecommendationRepository.upsert(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PendingRecommendationResponse response = useCase.execute(new GetDailyRecommendationRequest(1L, TODAY, true));

        assertThat(response.isNew()).isTrue();
        assertThat(response.decision()).isEqualTo(RecommendationResponseDecision.PENDING);
        assertThat(response.recommendedTaskIds()).containsExactly(2L);
        verify(pendingRecommendationRepository, never()).updateDecision(any(), any(), any());
    }

    private PendingTask task(Long id, double score) {
        return PendingTask.builder()
                .id(id).userId(1L).title("Tarea " + id).status(PendingStatus.PENDING)
                .mentalLoadScore(score).mentalLoadBucket(MentalLoadBucket.MEDIUM).build();
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
