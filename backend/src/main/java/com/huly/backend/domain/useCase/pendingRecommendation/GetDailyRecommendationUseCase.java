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
        if (pendingTasks.size() < MIN_PENDING_TASKS) {
            log.info("daily_recommendation_not_applicable userId={} date={} pendingTaskCount={}",
                    request.userId(), request.today(), pendingTasks.size());
            return mapper.notApplicable();
        }

        String currentHash = hash(pendingTasks);
        Optional<PendingDailyRecommendation> existing = pendingRecommendationRepository
                .findByUserIdAndDate(request.userId(), request.today());

        if (existing.isPresent() && existing.get().getPendingSetHash().equals(currentHash)) {
            PendingDailyRecommendation current = existing.get();
            if (request.forceRedecide() && current.getDecision() != RecommendationResponseDecision.PENDING) {
                log.info("daily_recommendation_redecide_reset userId={} date={} recommendationId={} previousDecision={} recommendedTaskIds={}",
                        request.userId(), request.today(), current.getId(), current.getDecision(), current.getRecommendedTaskIds());
                current = pendingRecommendationRepository.updateDecision(
                        current.getId(), RecommendationResponseDecision.PENDING, null);
            } else {
                log.info("daily_recommendation_reused userId={} date={} recommendationId={} decision={} candidateCount={} recommendedTaskIds={}",
                        request.userId(), request.today(), current.getId(), current.getDecision(), pendingTasks.size(), current.getRecommendedTaskIds());
            }
            return mapper.toResponse(current, false);
        }

        log.info("daily_recommendation_generating userId={} date={} budget={} candidates={}",
                request.userId(), request.today(), TaskBalanceRecommendationService.DAILY_CAPACITY_BUDGET,
                describeCandidates(pendingTasks));

        TaskBalanceRecommendationResult result = taskBalanceRecommendationService.recommend(
                pendingTasks, TaskBalanceRecommendationService.DAILY_CAPACITY_BUDGET, request.today());

        PendingDailyRecommendation toSave = PendingDailyRecommendation.builder()
                .userId(request.userId())
                .recommendationDate(request.today())
                .decision(RecommendationResponseDecision.PENDING)
                .pendingSetHash(currentHash)
                .totalLoadBudget(result.budget())
                .totalLoadUsed(result.totalLoadUsed())
                .recommendedTaskIds(result.recommendedTaskIds())
                .createdAt(Instant.now())
                .build();

        PendingDailyRecommendation saved;
        try {
            saved = pendingRecommendationRepository.upsert(toSave);
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
            log.info("daily_recommendation_concurrent_conflict_resolved userId={} date={}",
                    request.userId(), request.today());
            saved = pendingRecommendationRepository.findByUserIdAndDate(request.userId(), request.today())
                    .orElseThrow(() -> e);
            return mapper.toResponse(saved, false);
        }
        log.info("daily_recommendation_generated userId={} date={} recommendationId={} recommendedTaskIds={} usedBudget={} budget={}",
                request.userId(), request.today(), saved.getId(), saved.getRecommendedTaskIds(),
                saved.getTotalLoadUsed(), saved.getTotalLoadBudget());
        return mapper.toResponse(saved, true);
    }

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
