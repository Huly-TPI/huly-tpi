package com.huly.backend.domain.useCase.pendingRecommendation;

import com.huly.backend.domain.dto.pendingRecommendation.GetDailyRecommendationRequest;
import com.huly.backend.domain.dto.pendingRecommendation.PendingRecommendationResponse;
import com.huly.backend.domain.mapper.pendingRecommendation.PendingRecommendationMapper;
import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import com.huly.backend.domain.model.pending.PendingDailyRecommendation;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingRecommendationRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import com.huly.backend.domain.service.pending.TaskBalanceRecommendationResult;
import com.huly.backend.domain.service.pending.TaskBalanceRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.Instant.now;

@Slf4j
@RequiredArgsConstructor
public class GetDailyRecommendationUseCase {

    private static final int MIN_PENDING_TASKS = 2;

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingRecommendationRepository pendingRecommendationRepository;
    private final TaskBalanceRecommendationService taskBalanceRecommendationService;
    private final PendingRecommendationMapper mapper;

    public PendingRecommendationResponse execute(GetDailyRecommendationRequest request) {
        List<PendingTask> pendingTasks = pendingTaskRepository.findPendingByUserId(request.userId());
        if (isNotApplicable(request, pendingTasks))
            return mapper.notApplicable();

        String currentHash = hash(pendingTasks);
        Optional<PendingDailyRecommendation> existing = pendingRecommendationRepository
                .findByUserIdAndDate(request.userId(), request.today());

        if (existing.isPresent() && isRecommendationReusable(existing.get(), currentHash))
            return processExistingRecommendation(request, existing.get(), pendingTasks.size());

        return generateAndSaveNewRecommendation(request, pendingTasks, currentHash);
    }

    private boolean isNotApplicable(GetDailyRecommendationRequest request, List<PendingTask> pendingTasks) {
        if (pendingTasks.size() < MIN_PENDING_TASKS) {
            log.info("daily_recommendation_not_applicable userId={} date={} pendingTaskCount={}",
                    request.userId(), request.today(), pendingTasks.size());
            return true;
        }
        return false;
    }

    private boolean isRecommendationReusable(PendingDailyRecommendation recommendation, String currentHash) {
        return recommendation.getPendingSetHash().equals(currentHash);
    }

    private PendingRecommendationResponse processExistingRecommendation(
            GetDailyRecommendationRequest request, PendingDailyRecommendation existing, int candidateCount) {
        PendingDailyRecommendation current = existing;
        if (shouldResetDecision(request, current)) {
            logReset(request, current);
            current = pendingRecommendationRepository.updateDecision(
                    current.getId(), RecommendationResponseDecision.PENDING, null);
        } else {
            logReused(request, current, candidateCount);
        }
        return mapper.toResponse(current, false);
    }

    private boolean shouldResetDecision(GetDailyRecommendationRequest request, PendingDailyRecommendation current) {
        return request.forceRedecide() && current.getDecision() != RecommendationResponseDecision.PENDING;
    }

    private void logReset(GetDailyRecommendationRequest request, PendingDailyRecommendation current) {
        log.info("daily_recommendation_redecide_reset userId={} date={} recommendationId={} previousDecision={} recommendedTaskIds={}",
                request.userId(), request.today(), current.getId(), current.getDecision(), current.getRecommendedTaskIds());
    }

    private void logReused(GetDailyRecommendationRequest request, PendingDailyRecommendation current, int candidateCount) {
        log.info("daily_recommendation_reused userId={} date={} recommendationId={} decision={} candidateCount={} recommendedTaskIds={}",
                request.userId(), request.today(), current.getId(), current.getDecision(), candidateCount, current.getRecommendedTaskIds());
    }

    private PendingRecommendationResponse generateAndSaveNewRecommendation(
            GetDailyRecommendationRequest request, List<PendingTask> pendingTasks, String currentHash) {
        logGenerating(request, pendingTasks);

        TaskBalanceRecommendationResult result = taskBalanceRecommendationService.recommend(
                pendingTasks, TaskBalanceRecommendationService.DAILY_CAPACITY_BUDGET, request.today());

        PendingDailyRecommendation toSave = buildRecommendation(request, result, currentHash);
        SaveResult saveResult = upsertWithConflictResolution(toSave, request);

        if (saveResult.isNew()) {
            logGenerated(request, saveResult.recommendation());
        }
        return mapper.toResponse(saveResult.recommendation(), saveResult.isNew());
    }

    private void logGenerating(GetDailyRecommendationRequest request, List<PendingTask> pendingTasks) {
        log.info("daily_recommendation_generating userId={} date={} budget={} candidates={}",
                request.userId(), request.today(), TaskBalanceRecommendationService.DAILY_CAPACITY_BUDGET,
                describeCandidates(pendingTasks));
    }

    private PendingDailyRecommendation buildRecommendation(
            GetDailyRecommendationRequest request, TaskBalanceRecommendationResult result, String hash) {
        return PendingDailyRecommendation.builder()
                .userId(request.userId())
                .recommendationDate(request.today())
                .decision(RecommendationResponseDecision.PENDING)
                .pendingSetHash(hash)
                .totalLoadBudget(result.budget())
                .totalLoadUsed(result.totalLoadUsed())
                .recommendedTaskIds(result.recommendedTaskIds())
                .createdAt(now())
                .build();
    }

    private SaveResult upsertWithConflictResolution(
            PendingDailyRecommendation toSave, GetDailyRecommendationRequest request) {
        try {
            PendingDailyRecommendation saved = pendingRecommendationRepository.upsert(toSave);
            return new SaveResult(saved, true);
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
            log.info("daily_recommendation_concurrent_conflict_resolved userId={} date={}",
                    request.userId(), request.today());
            PendingDailyRecommendation saved = pendingRecommendationRepository.findByUserIdAndDate(request.userId(), request.today())
                    .orElseThrow(() -> e);
            return new SaveResult(saved, false);
        }
    }

    private void logGenerated(GetDailyRecommendationRequest request, PendingDailyRecommendation saved) {
        log.info("daily_recommendation_generated userId={} date={} recommendationId={} recommendedTaskIds={} usedBudget={} budget={}",
                request.userId(), request.today(), saved.getId(), saved.getRecommendedTaskIds(),
                saved.getTotalLoadUsed(), saved.getTotalLoadBudget());
    }

    private static record SaveResult(PendingDailyRecommendation recommendation, boolean isNew) {}

    private String describeCandidates(List<PendingTask> pendingTasks) {
        return pendingTasks.stream()
                .map(task -> task.getId() + "(score=" + scoreOf(task) + ",bucket=" + task.getMentalLoadBucket()
                        + ",dueDate=" + task.getDueDate() + ")")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String hash(List<PendingTask> pendingTasks) {
        String canonical = pendingTasks.stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(task -> task.getId() + ":" + scoreOf(task))
                .reduce((a, b) -> a + "|" + b)
                .orElse("");
        return sha256(canonical);
    }

    private double scoreOf(PendingTask task) {
        return task.getMentalLoadScore() == null ? 0.0 : task.getMentalLoadScore();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
