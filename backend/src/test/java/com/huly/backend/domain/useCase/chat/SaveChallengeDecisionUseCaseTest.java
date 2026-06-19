package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SaveChallengeDecisionUseCaseTest {

    @Mock
    private UserVectorMemoryService userVectorMemoryService;

    @InjectMocks
    private SaveChallengeDecisionUseCase useCase;

    @Test
    void execute_shouldSaveMemory_whenInputIsValid() {
        Long userId = 123L;
        String title = "Desafío de respiración";
        String decision = "ACCEPTED";
        String description = "Hacer 5 respiraciones lentas";
        String conversationId = "conv-abc";

        useCase.execute(userId, title, decision, description, conversationId);

        ArgumentCaptor<SaveVectorMemoryCommand> captor = ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());

        SaveVectorMemoryCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(command.contentType()).isEqualTo("CHALLENGE_DECISION");
        assertThat(command.content()).contains("acepto").contains(title).contains(description);
        assertThat(command.conversationId()).isEqualTo(conversationId);
        assertThat(command.metadata()).containsEntry("decision", "ACCEPTED");
        assertThat(command.metadata()).containsEntry("challengeTitle", title);
    }

    @Test
    void execute_shouldDoNothing_whenUserIdIsNull() {
        useCase.execute(null, "title", "ACCEPTED", "desc", "conv");
        verify(userVectorMemoryService, never()).saveMemory(ArgumentMatchers.any());
    }

    @Test
    void execute_shouldDoNothing_whenTitleIsEmpty() {
        useCase.execute(1L, "", "ACCEPTED", "desc", "conv");
        verify(userVectorMemoryService, never()).saveMemory(ArgumentMatchers.any());
    }

    @Test
    void execute_shouldDoNothing_whenDecisionIsEmpty() {
        useCase.execute(1L, "title", "", "desc", "conv");
        verify(userVectorMemoryService, never()).saveMemory(ArgumentMatchers.any());
    }
}
