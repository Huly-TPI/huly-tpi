package com.huly.backend.domain.useCase.journal;

import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.repository.journal.JournalEntryRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateJournalEntryUseCaseTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private UserVectorMemoryService userVectorMemoryService;

    @InjectMocks
    private CreateJournalEntryUseCase createJournalEntryUseCase;

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

        createJournalEntryUseCase.execute(1L, "Hoy me sentí bien", Mood.HAPPY, true);

        verify(journalEntryRepository).save(1L, "Hoy me sentí bien", Mood.HAPPY);
    }

    @Test
    void execute_shouldReturnWhatRepositoryReturns() {
        JournalEntry expected = buildEntry(10L, "Hoy me sentí bien", Mood.CALM);
        when(journalEntryRepository.save(1L, "Hoy me sentí bien", Mood.CALM)).thenReturn(expected);

        JournalEntry result = createJournalEntryUseCase.execute(1L, "Hoy me sentí bien", Mood.CALM, true);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void execute_shouldPassCorrectArgumentsToRepository() {
        when(journalEntryRepository.save(2L, "Entrada de prueba", Mood.ANXIOUS))
                .thenReturn(buildEntry(5L, "Entrada de prueba", Mood.ANXIOUS));

        createJournalEntryUseCase.execute(2L, "Entrada de prueba", Mood.ANXIOUS, true);

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

        JournalEntry result = createJournalEntryUseCase.execute(1L, "Sin mood", null, true);

        assertThat(result.getMood()).isNull();
        verify(journalEntryRepository).save(1L, "Sin mood", null);
    }


    @Test
    void execute_shouldCallRememberJournalEntry_afterSaving() {
        when(journalEntryRepository.save(1L, JSON_CONTENT, Mood.HAPPY))
                .thenReturn(buildEntry(10L, JSON_CONTENT, Mood.HAPPY));

        createJournalEntryUseCase.execute(1L, JSON_CONTENT, Mood.HAPPY, true);

        verify(userVectorMemoryService).rememberJournalEntry(eq(1L), eq(10L), anyString());
    }

    @Test
    void execute_shouldIncludeMoodInVectorContent() {
        when(journalEntryRepository.save(1L, JSON_CONTENT, Mood.HAPPY))
                .thenReturn(buildEntry(10L, JSON_CONTENT, Mood.HAPPY));

        ArgumentCaptor<String> vectorCaptor = ArgumentCaptor.forClass(String.class);
        createJournalEntryUseCase.execute(1L, JSON_CONTENT, Mood.HAPPY, true);

        verify(userVectorMemoryService).rememberJournalEntry(eq(1L), eq(10L), vectorCaptor.capture());
        assertThat(vectorCaptor.getValue()).contains("HAPPY");
    }

    @Test
    void execute_shouldIncludeAllJournalFieldsInVectorContent() {
        when(journalEntryRepository.save(1L, JSON_CONTENT, null))
                .thenReturn(buildEntry(10L, JSON_CONTENT, null));

        ArgumentCaptor<String> vectorCaptor = ArgumentCaptor.forClass(String.class);
        createJournalEntryUseCase.execute(1L, JSON_CONTENT, null, true);

        verify(userVectorMemoryService).rememberJournalEntry(eq(1L), eq(10L), vectorCaptor.capture());
        String vectorContent = vectorCaptor.getValue();
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

        ArgumentCaptor<String> vectorCaptor = ArgumentCaptor.forClass(String.class);
        createJournalEntryUseCase.execute(1L, plainContent, null, true);

        verify(userVectorMemoryService).rememberJournalEntry(eq(1L), eq(10L), vectorCaptor.capture());
        assertThat(vectorCaptor.getValue()).contains("Texto plano sin formato JSON");
    }

    @Test
    void execute_shouldOmitTextFromVectorContent_whenUseTextForAIIsFalse() {
        when(journalEntryRepository.save(1L, JSON_CONTENT, Mood.HAPPY))
                .thenReturn(buildEntry(10L, JSON_CONTENT, Mood.HAPPY));

        ArgumentCaptor<String> vectorCaptor = ArgumentCaptor.forClass(String.class);
        createJournalEntryUseCase.execute(1L, JSON_CONTENT, Mood.HAPPY, false);

        verify(userVectorMemoryService).rememberJournalEntry(eq(1L), eq(10L), vectorCaptor.capture());
        String vectorContent = vectorCaptor.getValue();
        assertThat(vectorContent).doesNotContain("Lo de adentro");
        assertThat(vectorContent).doesNotContain("Mi pensamiento");
        assertThat(vectorContent).doesNotContain("Algo bien");
        assertThat(vectorContent).doesNotContain("Para mañana");
    }

    @Test
    void execute_shouldIncludeMoodInVectorContent_whenUseTextForAIIsFalse() {
        when(journalEntryRepository.save(1L, JSON_CONTENT, Mood.CALM))
                .thenReturn(buildEntry(10L, JSON_CONTENT, Mood.CALM));

        ArgumentCaptor<String> vectorCaptor = ArgumentCaptor.forClass(String.class);
        createJournalEntryUseCase.execute(1L, JSON_CONTENT, Mood.CALM, false);

        verify(userVectorMemoryService).rememberJournalEntry(eq(1L), eq(10L), vectorCaptor.capture());
        assertThat(vectorCaptor.getValue()).contains("CALM");
    }

    @Test
    void execute_shouldOmitMoodFromVectorContent_whenMoodIsNull() {
        when(journalEntryRepository.save(1L, JSON_CONTENT, null))
                .thenReturn(buildEntry(10L, JSON_CONTENT, null));

        ArgumentCaptor<String> vectorCaptor = ArgumentCaptor.forClass(String.class);
        createJournalEntryUseCase.execute(1L, JSON_CONTENT, null, true);

        verify(userVectorMemoryService).rememberJournalEntry(eq(1L), eq(10L), vectorCaptor.capture());
        assertThat(vectorCaptor.getValue()).doesNotContain("Estado de ánimo");
    }
}
