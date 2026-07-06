package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.activities.ListActivitiesMapper;
import com.huly.backend.domain.mapper.activities.RegisterActivitySessionMapper;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import com.huly.backend.domain.useCase.activities.ListActivitiesUseCase;
import com.huly.backend.domain.useCase.activities.RegisterActivitySessionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivitiesUseCaseConfig {

    @Bean
    public ListActivitiesMapper listActivitiesMapper() {
        return new ListActivitiesMapper();
    }

    @Bean
    public RegisterActivitySessionMapper registerActivitySessionMapper() {
        return new RegisterActivitySessionMapper();
    }

    @Bean
    public ListActivitiesUseCase listActivitiesUseCase(ActivityRepository activityRepository,
                                                       ListActivitiesMapper listActivitiesMapper) {
        return new ListActivitiesUseCase(activityRepository, listActivitiesMapper);
    }

    @Bean
    public RegisterActivitySessionUseCase registerActivitySessionUseCase(ActivitySessionRepository activitySessionRepository,
                                                                         MandalaProgressRepository mandalaProgressRepository,
                                                                         RegisterActivitySessionMapper registerActivitySessionMapper) {
        return new RegisterActivitySessionUseCase(
                activitySessionRepository,
                mandalaProgressRepository,
                registerActivitySessionMapper);
    }
}
