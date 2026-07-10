package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.userGoal.AcceptChallengeMapper;
import com.huly.backend.domain.mapper.userGoal.AddUserGoalMapper;
import com.huly.backend.domain.mapper.userGoal.CompleteUserGoalMapper;
import com.huly.backend.domain.mapper.userGoal.DeleteUserGoalMapper;
import com.huly.backend.domain.mapper.userGoal.GetUserGoalsMapper;
import com.huly.backend.domain.mapper.userGoal.UpdateUserGoalMapper;
import com.huly.backend.domain.port.ImageValidationPort;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import com.huly.backend.domain.repository.UserPlantRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.service.userGoal.ImageStorageService;
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
    public AcceptChallengeMapper acceptChallengeMapper() {
        return new AcceptChallengeMapper();
    }

    @Bean
    public AddUserGoalMapper addUserGoalMapper() {
        return new AddUserGoalMapper();
    }

    @Bean
    public UpdateUserGoalMapper updateUserGoalMapper() {
        return new UpdateUserGoalMapper();
    }

    @Bean
    public DeleteUserGoalMapper deleteUserGoalMapper() {
        return new DeleteUserGoalMapper();
    }

    @Bean
    public GetUserGoalsMapper getUserGoalsMapper() {
        return new GetUserGoalsMapper();
    }

    @Bean
    public CompleteUserGoalMapper completeUserGoalMapper() {
        return new CompleteUserGoalMapper();
    }

    @Bean
    public AcceptChallengeUseCase acceptChallengeUseCase(UserGoalRepository userGoalRepository,
                                                         AcceptChallengeMapper acceptChallengeMapper) {
        return new AcceptChallengeUseCase(userGoalRepository, acceptChallengeMapper);
    }

    @Bean
    public AddUserGoalUseCase addUserGoalUseCase(UserGoalRepository userGoalRepository,
                                                 AddUserGoalMapper addUserGoalMapper) {
        return new AddUserGoalUseCase(userGoalRepository, addUserGoalMapper);
    }

    @Bean
    public CompleteUserGoalUseCase completeUserGoalUseCase(
            UserGoalRepository userGoalRepository,
            UserPlantRepository userPlantRepository,
            GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase,
            CoinService coinService,
            ImageStorageService imageStorageService,
            ImageValidationPort imageValidationPort,
            CompleteUserGoalMapper completeUserGoalMapper) {
        return new CompleteUserGoalUseCase(userGoalRepository, userPlantRepository,
                getOrCreateCurrentPlantUseCase, coinService, imageStorageService, imageValidationPort,
                completeUserGoalMapper);
    }

    @Bean
    public DeleteUserGoalUseCase deleteUserGoalUseCase(UserGoalRepository userGoalRepository,
                                                       DeleteUserGoalMapper deleteUserGoalMapper) {
        return new DeleteUserGoalUseCase(userGoalRepository, deleteUserGoalMapper);
    }

    @Bean
    public GetUserGoalsByUserUseCase getUserGoalsByUserUseCase(UserGoalRepository userGoalRepository,
                                                               GetUserGoalsMapper getUserGoalsMapper) {
        return new GetUserGoalsByUserUseCase(userGoalRepository, getUserGoalsMapper);
    }

    @Bean
    public UpdateUserGoalUseCase updateUserGoalUseCase(UserGoalRepository userGoalRepository,
                                                       UpdateUserGoalMapper updateUserGoalMapper) {
        return new UpdateUserGoalUseCase(userGoalRepository, updateUserGoalMapper);
    }
}
