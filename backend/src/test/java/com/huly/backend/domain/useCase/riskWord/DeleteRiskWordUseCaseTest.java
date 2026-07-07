package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.DeleteRiskWordRequest;
import com.huly.backend.domain.service.chat.RiskWordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteRiskWordUseCaseTest {

    private static final Long RISK_WORD_ID = 1L;

    @Mock
    private RiskWordService riskWordService;

    @InjectMocks
    private DeleteRiskWordUseCase deleteRiskWordUseCase;

    @Test
    @DisplayName("Delega la eliminación al servicio con el id recibido")
    void executeShouldDelegateDeletionToServiceWithId() {
        delete();

        thenServiceDeletedById();
    }

    // --- act ---

    private void delete() {
        deleteRiskWordUseCase.execute(new DeleteRiskWordRequest(RISK_WORD_ID));
    }

    // --- assert ---

    private void thenServiceDeletedById() {
        verify(riskWordService).delete(RISK_WORD_ID);
    }
}
