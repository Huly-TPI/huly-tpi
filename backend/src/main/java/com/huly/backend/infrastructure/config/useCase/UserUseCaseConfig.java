package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.user.GetCurrentMembershipMapper;
import com.huly.backend.domain.mapper.user.GetUserCoinsMapper;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.useCase.user.ChangePasswordUseCase;
import com.huly.backend.domain.useCase.user.GetUserAccountSettingsUseCase;
import com.huly.backend.domain.useCase.user.GetUserCoinsUseCase;
import com.huly.backend.domain.useCase.user.GetCurrentMembershipUseCase;
import com.huly.backend.domain.useCase.user.UpdateUserAccountSettingsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserUseCaseConfig {

    @Bean
    public GetUserCoinsMapper getUserCoinsMapper() {
        return new GetUserCoinsMapper();
    }

    @Bean
    public GetCurrentMembershipMapper getCurrentMembershipMapper() {
        return new GetCurrentMembershipMapper();
    }

    @Bean
    public GetUserCoinsUseCase getUserCoinsUseCase(UserRepository userRepository,
                                                   GetUserCoinsMapper getUserCoinsMapper) {
        return new GetUserCoinsUseCase(userRepository, getUserCoinsMapper);
    }

    @Bean
    public GetCurrentMembershipUseCase getCurrentMembershipUseCase(UserPlanRepository userPlanRepository,
                                                                   GetCurrentMembershipMapper getCurrentMembershipMapper) {
        return new GetCurrentMembershipUseCase(userPlanRepository, getCurrentMembershipMapper);
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(UserRepository userRepository,
                                                       PasswordHasherPort passwordHasherPort) {
        return new ChangePasswordUseCase(userRepository, passwordHasherPort);
    }

    @Bean
    public GetUserAccountSettingsUseCase getUserAccountSettingsUseCase(UserDetailDomainRepository userDetailDomainRepository) {
        return new GetUserAccountSettingsUseCase(userDetailDomainRepository);
    }

    @Bean
    public UpdateUserAccountSettingsUseCase updateUserAccountSettingsUseCase(UserDetailDomainRepository userDetailDomainRepository) {
        return new UpdateUserAccountSettingsUseCase(userDetailDomainRepository);
    }
}
