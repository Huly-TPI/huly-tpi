package com.huly.backend.domain.useCase.pendingRecommendation;

import com.huly.backend.domain.dto.pendingRecommendation.PendingRecommendationResponse;
import com.huly.backend.domain.dto.pendingRecommendation.RespondToRecommendationRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pendingRecommendation.PendingRecommendationMapper;
import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import com.huly.backend.domain.model.pending.PendingDailyRecommendation;
import com.huly.backend.domain.repository.pending.PendingRecommendationRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class RespondToRecommendationUseCase {

    private final PendingRecommendationRepository pendingRecommendationRepository;
    private final PendingRecommendationMapper mapper;

    public PendingRecommendationResponse execute(RespondToRecommendationRequest request) {
        PendingDailyRecommendation recommendation = pendingRecommendationRepository
                .findByIdAndUserId(request.recommendationId(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Recomendación", "id", request.recommendationId()));

        Instant now = Instant.now();
        if (request.decision() == RecommendationResponseDecision.ACCEPTED) {
            recommendation.accept(now);
        } else {
            recommendation.reject(now);
        }

        PendingDailyRecommendation updated = pendingRecommendationRepository.updateDecision(
                recommendation.getId(), recommendation.getDecision(), recommendation.getDecidedAt());
        return mapper.toResponse(updated, false);
    }
}
