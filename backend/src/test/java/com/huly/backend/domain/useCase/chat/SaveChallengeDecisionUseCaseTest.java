package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SaveChallengeDecisionUseCaseTest {

    private static final Long USER_ID = 123L;
    private static final String TITLE = "Desafío de respiración";
    private static final String DESCRIPTION = "Hacer 5 respiraciones lentas";
    private static final String CONVERSATION_ID = "conv-abc";

    @Mock
    private UserVectorMemoryService userVectorMemoryService;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private SaveChallengeDecisionUseCase useCase;

    private Long userId;
    private String title;
    private String decision;
    private String description;
    private String conversationId;

    @Test
    @DisplayName("Guarda la memoria de una decisión aceptada cuando la entrada es válida")
    void executeShouldSaveMemoryWhenInputIsValid() {
        // --- arrange ---
        givenInput(USER_ID, TITLE, "ACCEPTED", DESCRIPTION, CONVERSATION_ID);
        // --- act ---
        execute();
        // --- assert ---
        thenAcceptedMemorySaved();
    }

    @Test
    @DisplayName("Guarda la memoria de un rechazo normalizando la decisión y usando descripción vacía y source unknown")
    void executeShouldSaveRejectionNormalizingDecisionWhenDescriptionAndConversationAreNull() {
        // --- arrange ---
        givenInput(USER_ID, TITLE, "rejected", null, null);
        // --- act ---
        execute();
        // --- assert ---
        thenRejectionMemorySavedWithEmptyDescriptionAndUnknownSource();
    }

    @Test
    @DisplayName("Usa unknown en el sourceId cuando el id de conversación está en blanco")
    void executeShouldFallbackToUnknownSourceWhenConversationIdIsBlank() {
        // --- arrange ---
        givenInput(USER_ID, TITLE, "ACCEPTED", DESCRIPTION, "   ");
        // --- act ---
        execute();
        // --- assert ---
        thenMemorySavedWithUnknownSource();
    }

    @Test
    @DisplayName("No hace nada cuando el userId es nulo")
    void executeShouldDoNothingWhenUserIdIsNull() {
        // --- arrange ---
        givenInput(null, TITLE, "ACCEPTED", DESCRIPTION, CONVERSATION_ID);
        // --- act ---
        execute();
        // --- assert ---
        thenNothingWasSaved();
    }

    @Test
    @DisplayName("No hace nada cuando el título está vacío")
    void executeShouldDoNothingWhenTitleIsEmpty() {
        // --- arrange ---
        givenInput(USER_ID, "", "ACCEPTED", DESCRIPTION, CONVERSATION_ID);
        // --- act ---
        execute();
        // --- assert ---
        thenNothingWasSaved();
    }

    @Test
    @DisplayName("No hace nada cuando el título es nulo")
    void executeShouldDoNothingWhenTitleIsNull() {
        // --- arrange ---
        givenInput(USER_ID, null, "ACCEPTED", DESCRIPTION, CONVERSATION_ID);
        // --- act ---
        execute();
        // --- assert ---
        thenNothingWasSaved();
    }

    @Test
    @DisplayName("No hace nada cuando la decisión está vacía")
    void executeShouldDoNothingWhenDecisionIsEmpty() {
        // --- arrange ---
        givenInput(USER_ID, TITLE, "", DESCRIPTION, CONVERSATION_ID);
        // --- act ---
        execute();
        // --- assert ---
        thenNothingWasSaved();
    }

    @Test
    @DisplayName("No hace nada cuando la decisión es nula")
    void executeShouldDoNothingWhenDecisionIsNull() {
        // --- arrange ---
        givenInput(USER_ID, TITLE, null, DESCRIPTION, CONVERSATION_ID);
        // --- act ---
        execute();
        // --- assert ---
        thenNothingWasSaved();
    }

    // --- arrange ---

    private void givenInput(Long userId, String title, String decision, String description, String conversationId) {
        this.userId = userId;
        this.title = title;
        this.decision = decision;
        this.description = description;
        this.conversationId = conversationId;
    }

    // --- act ---

    private void execute() {
        useCase.execute(userId, title, decision, description, conversationId);
    }

    // --- assert ---

    private void thenAcceptedMemorySaved() {
        SaveVectorMemoryCommand command = captureSavedMemory();
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(command.contentType()).isEqualTo("CHALLENGE_DECISION");
        assertThat(command.content()).contains("acepto").contains(title).contains(description);
        assertThat(command.conversationId()).isEqualTo(conversationId);
        assertThat(command.metadata()).containsEntry("decision", "ACCEPTED");
        assertThat(command.metadata()).containsEntry("challengeTitle", title);
        verify(chatMessageRepository).updateChallengeDecision(conversationId, userId, title, description, "ACCEPTED");
    }

    private void thenRejectionMemorySavedWithEmptyDescriptionAndUnknownSource() {
        SaveVectorMemoryCommand command = captureSavedMemory();
        assertThat(command.content()).contains("rechazo").contains(title).contains("Descripcion: .");
        assertThat(command.sourceId()).contains("challenge-decision").contains("unknown").contains("REJECTED");
        assertThat(command.conversationId()).isNull();
        assertThat(command.metadata()).containsEntry("decision", "REJECTED");
        assertThat(command.metadata()).containsEntry("challengeDescription", "");
        verify(chatMessageRepository).updateChallengeDecision(null, userId, title, null, "REJECTED");
    }

    private void thenMemorySavedWithUnknownSource() {
        SaveVectorMemoryCommand command = captureSavedMemory();
        assertThat(command.sourceId()).contains("unknown");
        verify(chatMessageRepository).updateChallengeDecision(conversationId, userId, title, description, "ACCEPTED");
    }

    private void thenNothingWasSaved() {
        verify(userVectorMemoryService, never()).saveMemory(any());
        verify(chatMessageRepository, never()).updateChallengeDecision(any(), any(), any(), any(), any());
    }

    private SaveVectorMemoryCommand captureSavedMemory() {
        ArgumentCaptor<SaveVectorMemoryCommand> captor = ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        return captor.getValue();
    }
}
