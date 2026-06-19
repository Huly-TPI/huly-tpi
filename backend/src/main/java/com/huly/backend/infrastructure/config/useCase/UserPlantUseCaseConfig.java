package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.user.UserGoalRepository;
import com.huly.backend.domain.repository.UserPlantRepository;
import com.huly.backend.domain.useCase.userPlant.GetOrCreateCurrentPlantUseCase;
import com.huly.backend.domain.useCase.userPlant.GetPlantGoalsUseCase;
import com.huly.backend.domain.useCase.userPlant.GetUserPlantsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserPlantUseCaseConfig {

    @Bean
    public GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase(UserPlantRepository userPlantRepository) {
        return new GetOrCreateCurrentPlantUseCase(userPlantRepository);
    }

    @Bean
    public GetUserPlantsUseCase getUserPlantsUseCase(UserPlantRepository userPlantRepository) {
        return new GetUserPlantsUseCase(userPlantRepository);
    }

    @Bean
    public GetPlantGoalsUseCase getPlantGoalsUseCase(UserGoalRepository userGoalRepository) {
        return new GetPlantGoalsUseCase(userGoalRepository);
    }
}
