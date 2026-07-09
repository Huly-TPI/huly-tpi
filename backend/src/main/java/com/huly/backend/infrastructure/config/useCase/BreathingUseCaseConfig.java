package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.BreathingSession.GetBreathingTechniquesMapper;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import com.huly.backend.domain.useCase.breathingTechnique.GetBreathingTechniquesUseCase;
import com.huly.backend.domain.useCase.breathingTechnique.CreateBreathingTechniqueUseCase;
import com.huly.backend.domain.useCase.breathingTechnique.UpdateBreathingTechniqueUseCase;
import com.huly.backend.domain.useCase.breathingTechnique.SetBreathingTechniqueActiveUseCase;
import com.huly.backend.domain.useCase.breathingTechnique.ListBreathingTechniquesUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BreathingUseCaseConfig {

    @Bean
    public GetBreathingTechniquesMapper getBreathingTechniquesMapper() {
        return new GetBreathingTechniquesMapper();
    }

    @Bean
    public GetBreathingTechniquesUseCase getBreathingTechniquesUseCase(
            BreathingTechniqueRepository breathingTechniqueRepository,
            GetBreathingTechniquesMapper getBreathingTechniquesMapper) {
        return new GetBreathingTechniquesUseCase(breathingTechniqueRepository, getBreathingTechniquesMapper);
    }

    @Bean
    public ListBreathingTechniquesUseCase listBreathingTechniquesUseCase(BreathingTechniqueRepository repo) {
        return new ListBreathingTechniquesUseCase(repo);
    }

    @Bean
    public CreateBreathingTechniqueUseCase createBreathingTechniqueUseCase(BreathingTechniqueRepository repo) {
        return new CreateBreathingTechniqueUseCase(repo);
    }

    @Bean
    public UpdateBreathingTechniqueUseCase updateBreathingTechniqueUseCase(BreathingTechniqueRepository repo) {
        return new UpdateBreathingTechniqueUseCase(repo);
    }

    @Bean
    public SetBreathingTechniqueActiveUseCase setBreathingTechniqueActiveUseCase(BreathingTechniqueRepository repo) {
        return new SetBreathingTechniqueActiveUseCase(repo);
    }
}
