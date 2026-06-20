package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import com.huly.backend.domain.useCase.BreathingSession.GetBreathingTechniquesUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BreathingUseCaseConfig {

    @Bean
    public GetBreathingTechniquesUseCase getBreathingTechniquesUseCase(BreathingTechniqueRepository breathingTechniqueRepository) {
        return new GetBreathingTechniquesUseCase(breathingTechniqueRepository);
    }
}
