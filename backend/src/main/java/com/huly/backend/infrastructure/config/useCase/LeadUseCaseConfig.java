package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.lead.RegisterLeadMapper;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.useCase.lead.RegisterLeadUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LeadUseCaseConfig {

    @Bean
    public RegisterLeadMapper registerLeadMapper() {
        return new RegisterLeadMapper();
    }

    @Bean
    public RegisterLeadUseCase registerLeadUseCase(UserRepository userRepository,
                                                   EmailPort emailPort,
                                                   RegisterLeadMapper registerLeadMapper) {
        return new RegisterLeadUseCase(userRepository, emailPort, registerLeadMapper);
    }
}
