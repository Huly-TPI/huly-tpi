package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.BreathingSession.GetBreathingTechniquesMapper;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import com.huly.backend.domain.useCase.BreathingSession.GetBreathingTechniquesUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BreathingUseCaseConfig {

    @Bean
    public GetBreathingTechniquesMapper getBreathingTechniquesMapper() {
        return new GetBreathingTechniquesMapper();
    }

    @Bean
    public GetBreathingTechniquesUseCase getBreathingTechniquesUseCase(BreathingTechniqueRepository breathingTechniqueRepository,
                                                                       GetBreathingTechniquesMapper getBreathingTechniquesMapper) {
        return new GetBreathingTechniquesUseCase(breathingTechniqueRepository, getBreathingTechniquesMapper);
    }
}
