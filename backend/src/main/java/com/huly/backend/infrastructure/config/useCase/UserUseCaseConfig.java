package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.useCase.user.GetUserCoinsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserUseCaseConfig {

    @Bean
    public GetUserCoinsUseCase getUserCoinsUseCase(UserRepository userRepository) {
        return new GetUserCoinsUseCase(userRepository);
    }
}
