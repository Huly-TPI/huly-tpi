package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.pendingRecommendation.PendingRecommendationMapper;
import com.huly.backend.domain.repository.pending.PendingRecommendationRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import com.huly.backend.domain.service.pending.TaskBalanceRecommendationService;
import com.huly.backend.domain.useCase.pendingRecommendation.GetDailyRecommendationUseCase;
import com.huly.backend.domain.useCase.pendingRecommendation.RespondToRecommendationUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PendingRecommendationUseCaseConfig {

    @Bean
    public PendingRecommendationMapper pendingRecommendationMapper() {
        return new PendingRecommendationMapper();
    }

    @Bean
    public GetDailyRecommendationUseCase getDailyRecommendationUseCase(
            PendingTaskRepository pendingTaskRepository,
            PendingRecommendationRepository pendingRecommendationRepository,
            TaskBalanceRecommendationService taskBalanceRecommendationService,
            PendingRecommendationMapper pendingRecommendationMapper) {
        return new GetDailyRecommendationUseCase(
                pendingTaskRepository, pendingRecommendationRepository,
                taskBalanceRecommendationService, pendingRecommendationMapper);
    }

    @Bean
    public RespondToRecommendationUseCase respondToRecommendationUseCase(
            PendingRecommendationRepository pendingRecommendationRepository,
            PendingRecommendationMapper pendingRecommendationMapper) {
        return new RespondToRecommendationUseCase(pendingRecommendationRepository, pendingRecommendationMapper);
    }
}
