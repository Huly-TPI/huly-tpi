package com.huly.backend.domain.useCase.journal;

import com.huly.backend.domain.dto.journal.JournalEntryItem;
import com.huly.backend.domain.dto.journal.ListJournalEntriesRequest;
import com.huly.backend.domain.dto.journal.ListJournalEntriesResponse;
import com.huly.backend.domain.mapper.journal.ListJournalEntriesMapper;
import com.huly.backend.domain.model.journal.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.repository.journal.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListJournalEntriesUseCaseTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private ListJournalEntriesUseCase listJournalEntriesUseCase;

    @BeforeEach
    void setUp() {
        listJournalEntriesUseCase = new ListJournalEntriesUseCase(
                journalEntryRepository, new ListJournalEntriesMapper());
    }

    @Test
    void execute_shouldReturnEntriesFromRepository() {
        Long userId = 1L;
        List<JournalEntry> expected = List.of(
                JournalEntry.builder().id(2L).userId(userId).journalId(1L)
                        .content("Segunda entrada").mood(Mood.CALM).createdAt(Instant.now()).build(),
                JournalEntry.builder().id(1L).userId(userId).journalId(1L)
                        .content("Primera entrada").mood(Mood.HAPPY).createdAt(Instant.now()).build()
        );

        when(journalEntryRepository.findAllByUserId(userId)).thenReturn(expected);

        ListJournalEntriesResponse result = listJournalEntriesUseCase.execute(new ListJournalEntriesRequest(userId));

        assertThat(result.entries()).hasSize(2);
        assertThat(result.entries().get(0).id()).isEqualTo(2L);
        assertThat(result.entries().get(1).id()).isEqualTo(1L);
        verify(journalEntryRepository).findAllByUserId(userId);
    }

    @Test
    void execute_shouldReturnEmptyList_whenUserHasNoEntries() {
        Long userId = 99L;

        when(journalEntryRepository.findAllByUserId(userId)).thenReturn(List.of());

        ListJournalEntriesResponse result = listJournalEntriesUseCase.execute(new ListJournalEntriesRequest(userId));

        assertThat(result.entries()).isEmpty();
        verify(journalEntryRepository).findAllByUserId(userId);
    }

    @Test
    void execute_shouldDelegateToRepository() {
        Long userId = 5L;

        when(journalEntryRepository.findAllByUserId(userId)).thenReturn(List.of());

        listJournalEntriesUseCase.execute(new ListJournalEntriesRequest(userId));

        verify(journalEntryRepository).findAllByUserId(userId);
    }

    @Test
    void execute_shouldReturnEntryWithNullMood() {
        Long userId = 1L;
        JournalEntry entryWithoutMood = JournalEntry.builder()
                .id(3L).userId(userId).journalId(1L)
                .content("Sin estado de ánimo").mood(null).createdAt(Instant.now())
                .build();

        when(journalEntryRepository.findAllByUserId(userId)).thenReturn(List.of(entryWithoutMood));

        ListJournalEntriesResponse result = listJournalEntriesUseCase.execute(new ListJournalEntriesRequest(userId));

        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().get(0).mood()).isNull();
    }

    @Test
    void execute_shouldPropagateExceptionFromRepository() {
        Long userId = 1L;

        when(journalEntryRepository.findAllByUserId(userId))
                .thenThrow(new RuntimeException("error de repositorio"));

        assertThatThrownBy(() -> listJournalEntriesUseCase.execute(new ListJournalEntriesRequest(userId)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error de repositorio");
    }
}
