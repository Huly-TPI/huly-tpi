package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.LanternThoughtRepository;
import com.huly.backend.domain.useCase.lantern.CreateLanternThoughtUseCase;
import com.huly.backend.domain.useCase.lantern.ListLanternThoughtsUseCase;
import com.huly.backend.domain.useCase.lantern.MarkWorkedOnUseCase;
import com.huly.backend.domain.useCase.lantern.UpdateLanternStatusUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LanternUseCaseConfig {

    @Bean
    public CreateLanternThoughtUseCase createLanternThoughtUseCase(LanternThoughtRepository lanternThoughtRepository) {
        return new CreateLanternThoughtUseCase(lanternThoughtRepository);
    }

    @Bean
    public ListLanternThoughtsUseCase listLanternThoughtsUseCase(LanternThoughtRepository lanternThoughtRepository) {
        return new ListLanternThoughtsUseCase(lanternThoughtRepository);
    }

    @Bean
    public UpdateLanternStatusUseCase updateLanternStatusUseCase(LanternThoughtRepository lanternThoughtRepository) {
        return new UpdateLanternStatusUseCase(lanternThoughtRepository);
    }

    @Bean
    public MarkWorkedOnUseCase markWorkedOnUseCase(LanternThoughtRepository lanternThoughtRepository) {
        return new MarkWorkedOnUseCase(lanternThoughtRepository);
    }
}
