package com.huly.backend.domain.useCase.journal;

import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.repository.JournalEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private CreateJournalEntryUseCase createJournalEntryUseCase;

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

        createJournalEntryUseCase.execute(1L, "Hoy me sentí bien", Mood.HAPPY);

        verify(journalEntryRepository).save(1L, "Hoy me sentí bien", Mood.HAPPY);
    }

    @Test
    void execute_shouldReturnWhatRepositoryReturns() {
        JournalEntry expected = buildEntry(10L, "Hoy me sentí bien", Mood.CALM);
        when(journalEntryRepository.save(1L, "Hoy me sentí bien", Mood.CALM)).thenReturn(expected);

        JournalEntry result = createJournalEntryUseCase.execute(1L, "Hoy me sentí bien", Mood.CALM);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void execute_shouldPassCorrectArgumentsToRepository() {
        when(journalEntryRepository.save(2L, "Entrada de prueba", Mood.ANXIOUS))
                .thenReturn(buildEntry(5L, "Entrada de prueba", Mood.ANXIOUS));

        createJournalEntryUseCase.execute(2L, "Entrada de prueba", Mood.ANXIOUS);

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

        JournalEntry result = createJournalEntryUseCase.execute(1L, "Sin mood", null);

        assertThat(result.getMood()).isNull();
        verify(journalEntryRepository).save(1L, "Sin mood", null);
    }
}
