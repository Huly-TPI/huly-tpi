package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.riskWord.CreateRiskWordMapper;
import com.huly.backend.domain.mapper.riskWord.ListRiskWordsMapper;
import com.huly.backend.domain.mapper.riskWord.UpdateRiskWordMapper;
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
    public CreateRiskWordMapper createRiskWordMapper() {
        return new CreateRiskWordMapper();
    }

    @Bean
    public UpdateRiskWordMapper updateRiskWordMapper() {
        return new UpdateRiskWordMapper();
    }

    @Bean
    public ListRiskWordsMapper listRiskWordsMapper() {
        return new ListRiskWordsMapper();
    }

    @Bean
    public CreateRiskWordUseCase createRiskWordUseCase(RiskWordService riskWordService,
                                                       CreateRiskWordMapper createRiskWordMapper) {
        return new CreateRiskWordUseCase(riskWordService, createRiskWordMapper);
    }

    @Bean
    public DeleteRiskWordUseCase deleteRiskWordUseCase(RiskWordService riskWordService) {
        return new DeleteRiskWordUseCase(riskWordService);
    }

    @Bean
    public ListRiskWordsUseCase listRiskWordsUseCase(RiskWordService riskWordService,
                                                     ListRiskWordsMapper listRiskWordsMapper) {
        return new ListRiskWordsUseCase(riskWordService, listRiskWordsMapper);
    }

    @Bean
    public UpdateRiskWordUseCase updateRiskWordUseCase(RiskWordService riskWordService,
                                                       UpdateRiskWordMapper updateRiskWordMapper) {
        return new UpdateRiskWordUseCase(riskWordService, updateRiskWordMapper);
    }
}
