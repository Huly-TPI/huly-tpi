package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.service.chat.RiskWordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteRiskWordUseCaseTest {

    @Mock
    private RiskWordService riskWordService;

    @InjectMocks
    private DeleteRiskWordUseCase deleteRiskWordUseCase;

    @Test
    void execute_shouldDelegateToService() {
        deleteRiskWordUseCase.execute(1L);

        verify(riskWordService).delete(1L);
    }
}
