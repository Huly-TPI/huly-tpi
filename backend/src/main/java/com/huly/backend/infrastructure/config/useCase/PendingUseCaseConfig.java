package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.port.pending.MentalLoadEstimationPort;
import com.huly.backend.domain.repository.pending.PendingRecommendationRepository;
import com.huly.backend.domain.repository.pending.PendingSubtaskRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import com.huly.backend.domain.service.pending.PendingMentalLoadRefreshService;
import com.huly.backend.domain.useCase.pending.AddPendingSubtaskUseCase;
import com.huly.backend.domain.useCase.pending.CompletePendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.CreatePendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.DeletePendingSubtaskUseCase;
import com.huly.backend.domain.useCase.pending.DeletePendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.GetPendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.ListPendingTasksUseCase;
import com.huly.backend.domain.useCase.pending.TogglePendingSubtaskUseCase;
import com.huly.backend.domain.useCase.pending.UpdatePendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.UpdatePendingPositionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadLocalRandom;

@Configuration
public class PendingUseCaseConfig {

    @Bean
    public PendingTaskMapper pendingTaskMapper() {
        return new PendingTaskMapper();
    }

    @Bean
    public PendingMentalLoadRefreshService pendingMentalLoadRefreshService(
            MentalLoadEstimationPort mentalLoadEstimationPort,
            PendingTaskRepository pendingTaskRepository) {
        return new PendingMentalLoadRefreshService(mentalLoadEstimationPort, pendingTaskRepository);
    }

    @Bean
    public CreatePendingTaskUseCase createPendingTaskUseCase(
            PendingTaskRepository pendingTaskRepository,
            PendingMentalLoadRefreshService pendingMentalLoadRefreshService,
            PendingTaskMapper pendingTaskMapper) {
        return new CreatePendingTaskUseCase(pendingTaskRepository, pendingMentalLoadRefreshService, pendingTaskMapper);
    }

    @Bean
    public ListPendingTasksUseCase listPendingTasksUseCase(
            PendingTaskRepository pendingTaskRepository,
            PendingRecommendationRepository pendingRecommendationRepository,
            PendingTaskMapper pendingTaskMapper) {
        return new ListPendingTasksUseCase(pendingTaskRepository, pendingRecommendationRepository, pendingTaskMapper);
    }

    @Bean
    public GetPendingTaskUseCase getPendingTaskUseCase(
            PendingTaskRepository pendingTaskRepository,
            PendingRecommendationRepository pendingRecommendationRepository,
            PendingTaskMapper pendingTaskMapper) {
        return new GetPendingTaskUseCase(pendingTaskRepository, pendingRecommendationRepository, pendingTaskMapper);
    }

    @Bean
    public UpdatePendingTaskUseCase updatePendingTaskUseCase(
            PendingTaskRepository pendingTaskRepository,
            PendingMentalLoadRefreshService pendingMentalLoadRefreshService,
            PendingTaskMapper pendingTaskMapper) {
        return new UpdatePendingTaskUseCase(pendingTaskRepository, pendingMentalLoadRefreshService, pendingTaskMapper);
    }

    @Bean
    public DeletePendingTaskUseCase deletePendingTaskUseCase(PendingTaskRepository pendingTaskRepository) {
        return new DeletePendingTaskUseCase(pendingTaskRepository);
    }

    @Bean
    public CompletePendingTaskUseCase completePendingTaskUseCase(
            PendingTaskRepository pendingTaskRepository,
            PendingTaskMapper pendingTaskMapper) {
        return new CompletePendingTaskUseCase(pendingTaskRepository, pendingTaskMapper);
    }

    @Bean
    public AddPendingSubtaskUseCase addPendingSubtaskUseCase(
            PendingTaskRepository pendingTaskRepository,
            PendingSubtaskRepository pendingSubtaskRepository,
            PendingMentalLoadRefreshService pendingMentalLoadRefreshService,
            PendingTaskMapper pendingTaskMapper) {
        return new AddPendingSubtaskUseCase(pendingTaskRepository, pendingSubtaskRepository, pendingMentalLoadRefreshService, pendingTaskMapper);
    }

    @Bean
    public TogglePendingSubtaskUseCase togglePendingSubtaskUseCase(
            PendingTaskRepository pendingTaskRepository,
            PendingSubtaskRepository pendingSubtaskRepository,
            PendingTaskMapper pendingTaskMapper) {
        return new TogglePendingSubtaskUseCase(pendingTaskRepository, pendingSubtaskRepository, pendingTaskMapper);
    }

    @Bean
    public DeletePendingSubtaskUseCase deletePendingSubtaskUseCase(
            PendingTaskRepository pendingTaskRepository,
            PendingSubtaskRepository pendingSubtaskRepository,
            PendingMentalLoadRefreshService pendingMentalLoadRefreshService) {
        return new DeletePendingSubtaskUseCase(pendingTaskRepository, pendingSubtaskRepository, pendingMentalLoadRefreshService);
    }

    @Bean
    public UpdatePendingPositionUseCase updatePendingPositionUseCase(
            PendingTaskRepository pendingTaskRepository,
            PendingTaskMapper pendingTaskMapper) {
        return new UpdatePendingPositionUseCase(
                pendingTaskRepository,
                pendingTaskMapper,
                () -> ThreadLocalRandom.current().nextDouble(-6, 6));
    }
}
