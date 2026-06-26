package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.userPlant.GetOrCreateCurrentPlantMapper;
import com.huly.backend.domain.mapper.userPlant.GetPlantGoalsMapper;
import com.huly.backend.domain.mapper.userPlant.GetUserPlantsMapper;
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
    public GetOrCreateCurrentPlantMapper getOrCreateCurrentPlantMapper() {
        return new GetOrCreateCurrentPlantMapper();
    }

    @Bean
    public GetUserPlantsMapper getUserPlantsMapper() {
        return new GetUserPlantsMapper();
    }

    @Bean
    public GetPlantGoalsMapper getPlantGoalsMapper() {
        return new GetPlantGoalsMapper();
    }

    @Bean
    public GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase(
            UserPlantRepository userPlantRepository,
            GetOrCreateCurrentPlantMapper getOrCreateCurrentPlantMapper) {
        return new GetOrCreateCurrentPlantUseCase(userPlantRepository, getOrCreateCurrentPlantMapper);
    }

    @Bean
    public GetUserPlantsUseCase getUserPlantsUseCase(UserPlantRepository userPlantRepository,
                                                     GetUserPlantsMapper getUserPlantsMapper) {
        return new GetUserPlantsUseCase(userPlantRepository, getUserPlantsMapper);
    }

    @Bean
    public GetPlantGoalsUseCase getPlantGoalsUseCase(UserGoalRepository userGoalRepository,
                                                     GetPlantGoalsMapper getPlantGoalsMapper) {
        return new GetPlantGoalsUseCase(userGoalRepository, getPlantGoalsMapper);
    }
}
