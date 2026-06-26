package com.huly.backend.domain.useCase.journal;

import com.huly.backend.domain.dto.journal.CreateJournalEntryRequest;
import com.huly.backend.domain.dto.journal.CreateJournalEntryResponse;
import com.huly.backend.domain.mapper.journal.CreateJournalEntryMapper;
import com.huly.backend.domain.model.journal.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.repository.journal.JournalEntryRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateJournalEntryUseCaseTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private UserVectorMemoryService userVectorMemoryService;

    private CreateJournalEntryUseCase createJournalEntryUseCase;

    @BeforeEach
    void setUp() {
        createJournalEntryUseCase = new CreateJournalEntryUseCase(
                journalEntryRepository, userVectorMemoryService, new CreateJournalEntryMapper());
    }

    private static final String JSON_CONTENT =
            "{\"adentro\":\"Lo de adentro\",\"pensamiento\":\"Mi pensamiento\",\"bien\":\"Algo bien\",\"manana\":\"Para mañana\"}";

    private JournalEntry buildEntry(Long id, String content, Mood mood) {
        return JournalEntry.builder()
                .id(id)
                .userId(1L)
                .journalId(1L)
                .content(content)
                .mood(mood)
                .createdAt(Instant.now())
                .build();
    }


    @Test
    void execute_shouldDelegateToRepository() {
        when(journalEntryRepository.save(1L, "Hoy me sentí bien", Mood.HAPPY))
                .thenReturn(buildEntry(10L, "Hoy me sentí bien", Mood.HAPPY));

        createJournalEntryUseCase.execute(new CreateJournalEntryRequest(1L, "Hoy me sentí bien", Mood.HAPPY, true));

        verify(journalEntryRepository).save(1L, "Hoy me sentí bien", Mood.HAPPY);
    }

    @Test
    void execute_shouldReturnWhatRepositoryReturns() {
        JournalEntry expected = buildEntry(10L, "Hoy me sentí bien", Mood.CALM);
        when(journalEntryRepository.save(1L, "Hoy me sentí bien", Mood.CALM)).thenReturn(expected);

        CreateJournalEntryResponse result =
                createJournalEntryUseCase.execute(new CreateJournalEntryRequest(1L, "Hoy me sentí bien", Mood.CALM, true));

        assertThat(result.id()).isEqualTo(expected.getId());
        assertThat(result.content()).isEqualTo(expected.getContent());
        assertThat(result.mood()).isEqualTo(expected.getMood());
        assertThat(result.createdAt()).isEqualTo(expected.getCreatedAt());
    }

    @Test
    void execute_shouldPassCorrectArgumentsToRepository() {
        when(journalEntryRepository.save(2L, "Entrada de prueba", Mood.ANXIOUS))
                .thenReturn(buildEntry(5L, "Entrada de prueba", Mood.ANXIOUS));

        createJournalEntryUseCase.execute(new CreateJournalEntryRequest(2L, "Entrada de prueba", Mood.ANXIOUS, true));

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Mood> moodCaptor = ArgumentCaptor.forClass(Mood.class);

        verify(journalEntryRepository).save(userIdCaptor.capture(), contentCaptor.capture(), moodCaptor.capture());

        assertThat(userIdCaptor.getValue()).isEqualTo(2L);
        assertThat(contentCaptor.getValue()).isEqualTo("Entrada de prueba");
        assertThat(moodCaptor.getValue()).isEqualTo(Mood.ANXIOUS);
    }

    @Test
    void execute_shouldAllowNullMood() {
        when(journalEntryRepository.save(1L, "Sin mood", null))
                .thenReturn(buildEntry(1L, "Sin mood", null));

        CreateJournalEntryResponse result =
                createJournalEntryUseCase.execute(new CreateJournalEntryRequest(1L, "Sin mood", null, true));

        assertThat(result.mood()).isNull();
        verify(journalEntryRepository).save(1L, "Sin mood", null);
    }


    @Test
    void execute_shouldCallRememberJournalEntry_afterSaving() {
        when(journalEntryRepository.save(1L, JSON_CONTENT, Mood.HAPPY))
                .thenReturn(buildEntry(10L, JSON_CONTENT, Mood.HAPPY));

        createJournalEntryUseCase.execute(new CreateJournalEntryRequest(1L, JSON_CONTENT, Mood.HAPPY, true));

        ArgumentCaptor<SaveVectorMemoryCommand> captor =
                ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(1L);
        assertThat(captor.getValue().sourceId()).isEqualTo("10");
    }

    @Test
    void execute_shouldIncludeMoodInVectorContent() {
        when(journalEntryRepository.save(1L, JSON_CONTENT, Mood.HAPPY))
                .thenReturn(buildEntry(10L, JSON_CONTENT, Mood.HAPPY));

        createJournalEntryUseCase.execute(new CreateJournalEntryRequest(1L, JSON_CONTENT, Mood.HAPPY, true));

        ArgumentCaptor<SaveVectorMemoryCommand> captor =
                ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        assertThat(captor.getValue().content()).contains("HAPPY");
    }

    @Test
    void execute_shouldIncludeAllJournalFieldsInVectorContent() {
        when(journalEntryRepository.save(1L, JSON_CONTENT, null))
                .thenReturn(buildEntry(10L, JSON_CONTENT, null));

        createJournalEntryUseCase.execute(new CreateJournalEntryRequest(1L, JSON_CONTENT, null, true));

        ArgumentCaptor<SaveVectorMemoryCommand> captor =
                ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        String vectorContent = captor.getValue().content();
        assertThat(vectorContent).contains("Lo de adentro");
        assertThat(vectorContent).contains("Mi pensamiento");
        assertThat(vectorContent).contains("Algo bien");
        assertThat(vectorContent).contains("Para mañana");
    }

    @Test
    void execute_shouldUsePlainTextFallback_whenContentIsNotJson() {
        String plainContent = "Texto plano sin formato JSON";
        when(journalEntryRepository.save(1L, plainContent, null))
                .thenReturn(buildEntry(10L, plainContent, null));

        createJournalEntryUseCase.execute(new CreateJournalEntryRequest(1L, plainContent, null, true));

        ArgumentCaptor<SaveVectorMemoryCommand> captor =
                ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        assertThat(captor.getValue().content()).contains("Texto plano sin formato JSON");
    }

    @Test
    void execute_shouldOmitTextFromVectorContent_whenUseTextForAIIsFalse() {
        when(journalEntryRepository.save(1L, JSON_CONTENT, Mood.HAPPY))
                .thenReturn(buildEntry(10L, JSON_CONTENT, Mood.HAPPY));

        createJournalEntryUseCase.execute(new CreateJournalEntryRequest(1L, JSON_CONTENT, Mood.HAPPY, false));

        ArgumentCaptor<SaveVectorMemoryCommand> captor =
                ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        String vectorContent = captor.getValue().content();
        assertThat(vectorContent).doesNotContain("Lo de adentro");
        assertThat(vectorContent).doesNotContain("Mi pensamiento");
        assertThat(vectorContent).doesNotContain("Algo bien");
        assertThat(vectorContent).doesNotContain("Para mañana");
    }

    @Test
    void execute_shouldIncludeMoodInVectorContent_whenUseTextForAIIsFalse() {
        when(journalEntryRepository.save(1L, JSON_CONTENT, Mood.CALM))
                .thenReturn(buildEntry(10L, JSON_CONTENT, Mood.CALM));

        createJournalEntryUseCase.execute(new CreateJournalEntryRequest(1L, JSON_CONTENT, Mood.CALM, false));

        ArgumentCaptor<SaveVectorMemoryCommand> captor =
                ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        assertThat(captor.getValue().content()).contains("CALM");
    }

    @Test
    void execute_shouldOmitMoodFromVectorContent_whenMoodIsNull() {
        when(journalEntryRepository.save(1L, JSON_CONTENT, null))
                .thenReturn(buildEntry(10L, JSON_CONTENT, null));

        createJournalEntryUseCase.execute(new CreateJournalEntryRequest(1L, JSON_CONTENT, null, true));

        ArgumentCaptor<SaveVectorMemoryCommand> captor =
                ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        assertThat(captor.getValue().content()).doesNotContain("Estado de ánimo");
    }
}
