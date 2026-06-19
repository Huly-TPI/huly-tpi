package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.port.TokenPort;
import com.huly.backend.domain.repository.auth.RefreshTokenRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.useCase.auth.AdminLoginUseCase;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import com.huly.backend.domain.useCase.auth.LoginUseCase;
import com.huly.backend.domain.useCase.auth.LogoutUseCase;
import com.huly.backend.domain.useCase.auth.RefreshTokenUseCase;
import com.huly.backend.domain.useCase.auth.RegisterUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthUseCaseConfig {

    @Bean
    public LoginUseCase loginUseCase(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, TokenPort tokenPort, PasswordHasherPort passwordHasherPort, UserDetailDomainRepository userDetailDomainRepository) {
        return new LoginUseCase(userRepository, refreshTokenRepository, tokenPort, passwordHasherPort, userDetailDomainRepository);
    }

    @Bean
    public AdminLoginUseCase adminLoginUseCase(UserRepository userRepository, LoginUseCase loginUseCase) {
        return new AdminLoginUseCase(userRepository, loginUseCase);
    }

    @Bean
    public RegisterUseCase registerUseCase(UserRepository userRepository, PasswordHasherPort passwordHasherPort, LoginUseCase loginUseCase) {
        return new RegisterUseCase(userRepository, passwordHasherPort, loginUseCase);
    }

    @Bean
    public LogoutUseCase logoutUseCase(RefreshTokenRepository refreshTokenRepository) {
        return new LogoutUseCase(refreshTokenRepository);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, TokenPort tokenPort) {
        return new RefreshTokenUseCase(userRepository, refreshTokenRepository, tokenPort);
    }

    @Bean
    public GetCurrentUserUseCase getCurrentUserUseCase(UserRepository userRepository, UserDetailDomainRepository userDetailDomainRepository) {
        return new GetCurrentUserUseCase(userRepository, userDetailDomainRepository);
    }
}
