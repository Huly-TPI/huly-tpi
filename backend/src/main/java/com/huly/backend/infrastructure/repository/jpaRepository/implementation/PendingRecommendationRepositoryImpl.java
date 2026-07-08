package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import com.huly.backend.domain.model.pending.PendingDailyRecommendation;
import com.huly.backend.domain.repository.pending.PendingRecommendationRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.PendingDailyRecommendationEntity;
import com.huly.backend.infrastructure.repository.entity.PendingDailyRecommendationTaskEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPendingDailyRecommendationJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPendingDailyRecommendationTaskJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPendingTaskJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PendingRecommendationRepositoryImpl implements PendingRecommendationRepository {

    private final IPendingDailyRecommendationJpaRepository jpaRepository;
    private final IPendingDailyRecommendationTaskJpaRepository taskJoinJpaRepository;
    private final IPendingTaskJpaRepository taskJpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public Optional<PendingDailyRecommendation> findByUserIdAndDate(Long userId, LocalDate date) {
        return jpaRepository.findByUser_IdAndRecommendationDate(userId, date).map(this::toDomain);
    }

    @Override
    public Optional<PendingDailyRecommendation> findByIdAndUserId(Long id, Long userId) {
        return jpaRepository.findByIdAndUser_Id(id, userId).map(this::toDomain);
    }

    @Override
    @Transactional
    public PendingDailyRecommendation upsert(PendingDailyRecommendation recommendation) {
        Optional<PendingDailyRecommendationEntity> existing = jpaRepository.findByUser_IdAndRecommendationDate(
                recommendation.getUserId(), recommendation.getRecommendationDate());

        PendingDailyRecommendationEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setDecision(RecommendationResponseDecision.PENDING);
            entity.setPendingSetHash(recommendation.getPendingSetHash());
            entity.setTotalLoadBudget(recommendation.getTotalLoadBudget());
            entity.setTotalLoadUsed(recommendation.getTotalLoadUsed());
            entity.setDecidedAt(null);
            taskJoinJpaRepository.deleteAllByRecommendation_Id(entity.getId());
            taskJoinJpaRepository.flush();
        } else {
            AppUserEntity user = appUserRepository.getReferenceById(recommendation.getUserId());
            entity = PendingDailyRecommendationEntity.builder()
                    .user(user)
                    .recommendationDate(recommendation.getRecommendationDate())
                    .decision(RecommendationResponseDecision.PENDING)
                    .pendingSetHash(recommendation.getPendingSetHash())
                    .totalLoadBudget(recommendation.getTotalLoadBudget())
                    .totalLoadUsed(recommendation.getTotalLoadUsed())
                    .createdAt(Instant.now())
                    .build();
        }

        PendingDailyRecommendationEntity saved = jpaRepository.save(entity);

        List<PendingDailyRecommendationTaskEntity> joinRows = recommendation.getRecommendedTaskIds().stream()
                .map(taskId -> PendingDailyRecommendationTaskEntity.builder()
                        .recommendation(saved)
                        .task(taskJpaRepository.getReferenceById(taskId))
                        .build())
                .toList();
        taskJoinJpaRepository.saveAll(joinRows);

        return toDomain(saved, recommendation.getRecommendedTaskIds());
    }

    @Override
    public PendingDailyRecommendation updateDecision(Long recommendationId, RecommendationResponseDecision decision, Instant decidedAt) {
        PendingDailyRecommendationEntity entity = jpaRepository.findById(recommendationId)
                .orElseThrow(() -> new NotFoundException("Recomendación", "id", recommendationId));
        entity.setDecision(decision);
        entity.setDecidedAt(decidedAt);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Set<Long> findAcceptedTaskIds(Long userId, LocalDate date) {
        Optional<PendingDailyRecommendationEntity> recommendation = jpaRepository.findByUser_IdAndRecommendationDate(userId, date);
        if (recommendation.isEmpty() || recommendation.get().getDecision() != RecommendationResponseDecision.ACCEPTED) {
            return Set.of();
        }
        return taskJoinJpaRepository.findAllByRecommendation_Id(recommendation.get().getId()).stream()
                .map(join -> join.getTask().getId())
                .collect(Collectors.toSet());
    }

    private PendingDailyRecommendation toDomain(PendingDailyRecommendationEntity entity) {
        List<Long> taskIds = taskJoinJpaRepository.findAllByRecommendation_Id(entity.getId()).stream()
                .map(join -> join.getTask().getId())
                .toList();
        return toDomain(entity, taskIds);
    }

    private PendingDailyRecommendation toDomain(PendingDailyRecommendationEntity entity, List<Long> recommendedTaskIds) {
        return PendingDailyRecommendation.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .recommendationDate(entity.getRecommendationDate())
                .decision(entity.getDecision())
                .pendingSetHash(entity.getPendingSetHash())
                .totalLoadBudget(entity.getTotalLoadBudget())
                .totalLoadUsed(entity.getTotalLoadUsed())
                .recommendedTaskIds(new java.util.ArrayList<>(recommendedTaskIds))
                .createdAt(entity.getCreatedAt())
                .decidedAt(entity.getDecidedAt())
                .build();
    }
}
