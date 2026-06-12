package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.domain.repository.UserPlantRepository;
import com.huly.backend.domain.useCase.userGoal.AcceptChallengeUseCase;
import com.huly.backend.domain.useCase.userGoal.AddUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.CompleteUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.DeleteUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.GetUserGoalsByUserUseCase;
import com.huly.backend.domain.useCase.userGoal.UpdateUserGoalUseCase;
import com.huly.backend.domain.useCase.userPlant.GetOrCreateCurrentPlantUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserGoalUseCaseConfig {

    @Bean
    public AcceptChallengeUseCase acceptChallengeUseCase(UserGoalRepository userGoalRepository) {
        return new AcceptChallengeUseCase(userGoalRepository);
    }

    @Bean
    public AddUserGoalUseCase addUserGoalUseCase(UserGoalRepository userGoalRepository) {
        return new AddUserGoalUseCase(userGoalRepository);
    }

    @Bean
    public CompleteUserGoalUseCase completeUserGoalUseCase(
            UserGoalRepository userGoalRepository,
            UserPlantRepository userPlantRepository,
            GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase) {
        return new CompleteUserGoalUseCase(userGoalRepository, userPlantRepository, getOrCreateCurrentPlantUseCase);
    }

    @Bean
    public DeleteUserGoalUseCase deleteUserGoalUseCase(UserGoalRepository userGoalRepository) {
        return new DeleteUserGoalUseCase(userGoalRepository);
    }

    @Bean
    public GetUserGoalsByUserUseCase getUserGoalsByUserUseCase(UserGoalRepository userGoalRepository) {
        return new GetUserGoalsByUserUseCase(userGoalRepository);
    }

    @Bean
    public UpdateUserGoalUseCase updateUserGoalUseCase(UserGoalRepository userGoalRepository) {
        return new UpdateUserGoalUseCase(userGoalRepository);
    }
}
