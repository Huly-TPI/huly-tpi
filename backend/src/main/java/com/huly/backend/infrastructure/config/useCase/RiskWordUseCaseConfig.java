package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.service.chat.RiskWordService;
import com.huly.backend.domain.useCase.riskWord.CreateRiskWordUseCase;
import com.huly.backend.domain.useCase.riskWord.DeleteRiskWordUseCase;
import com.huly.backend.domain.useCase.riskWord.ListRiskWordsUseCase;
import com.huly.backend.domain.useCase.riskWord.UpdateRiskWordUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RiskWordUseCaseConfig {

    @Bean
    public CreateRiskWordUseCase createRiskWordUseCase(RiskWordService riskWordService) {
        return new CreateRiskWordUseCase(riskWordService);
    }

    @Bean
    public DeleteRiskWordUseCase deleteRiskWordUseCase(RiskWordService riskWordService) {
        return new DeleteRiskWordUseCase(riskWordService);
    }

    @Bean
    public ListRiskWordsUseCase listRiskWordsUseCase(RiskWordService riskWordService) {
        return new ListRiskWordsUseCase(riskWordService);
    }

    @Bean
    public UpdateRiskWordUseCase updateRiskWordUseCase(RiskWordService riskWordService) {
        return new UpdateRiskWordUseCase(riskWordService);
    }
}
