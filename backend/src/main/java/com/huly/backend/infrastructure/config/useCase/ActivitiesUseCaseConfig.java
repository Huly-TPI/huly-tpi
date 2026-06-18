package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.ActivityRepository;
import com.huly.backend.domain.repository.ActivitySessionRepository;
import com.huly.backend.domain.useCase.activities.ListActivitiesUseCase;
import com.huly.backend.domain.useCase.activities.RegisterActivitySessionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivitiesUseCaseConfig {

    @Bean
    public ListActivitiesUseCase listActivitiesUseCase(ActivityRepository activityRepository) {
        return new ListActivitiesUseCase(activityRepository);
    }

    @Bean
    public RegisterActivitySessionUseCase registerActivitySessionUseCase(ActivitySessionRepository activitySessionRepository) {
        return new RegisterActivitySessionUseCase(activitySessionRepository);
    }
}
