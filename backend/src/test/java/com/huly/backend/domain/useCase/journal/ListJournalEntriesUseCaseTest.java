package com.huly.backend.domain.useCase.journal;

import com.huly.backend.domain.dto.journal.ListJournalEntriesRequest;
import com.huly.backend.domain.dto.journal.ListJournalEntriesResponse;
import com.huly.backend.domain.mapper.journal.ListJournalEntriesMapper;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.model.journal.JournalEntry;
import com.huly.backend.domain.repository.journal.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    private static final Long USER_ID = 1L;
    private static final Long EMPTY_USER_ID = 99L;
    private static final Long OTHER_USER_ID = 5L;
    private static final Long JOURNAL_ID = 1L;
    private static final Instant CREATED_AT = Instant.parse("2026-07-06T10:00:00Z");
    private static final String REPOSITORY_ERROR = "error de repositorio";

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private ListJournalEntriesUseCase listJournalEntriesUseCase;

    private ListJournalEntriesRequest request;

    @BeforeEach
    void setUp() {
        listJournalEntriesUseCase = new ListJournalEntriesUseCase(
                journalEntryRepository, new ListJournalEntriesMapper());
    }

    @Test
    @DisplayName("Devuelve las entradas provistas por el repositorio respetando el orden")
    void executeShouldReturnEntriesFromRepository() {
        // --- arrange ---
        givenRequestForUser(USER_ID);
        givenRepositoryReturnsTwoEntries();

        // --- act ---
        ListJournalEntriesResponse result = list();

        // --- assert ---
        thenResponseHasEntriesInOrder(result, 2L, 1L);
        thenRepositoryQueriedForUser(USER_ID);
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando el usuario no tiene entradas")
    void executeShouldReturnEmptyListWhenUserHasNoEntries() {
        // --- arrange ---
        givenRequestForUser(EMPTY_USER_ID);
        givenRepositoryReturnsNoEntries();

        // --- act ---
        ListJournalEntriesResponse result = list();

        // --- assert ---
        thenResponseIsEmpty(result);
        thenRepositoryQueriedForUser(EMPTY_USER_ID);
    }

    @Test
    @DisplayName("Delega la consulta al repositorio")
    void executeShouldDelegateToRepository() {
        // --- arrange ---
        givenRequestForUser(OTHER_USER_ID);
        givenRepositoryReturnsNoEntries();

        // --- act ---
        list();

        // --- assert ---
        thenRepositoryQueriedForUser(OTHER_USER_ID);
    }

    @Test
    @DisplayName("Devuelve una entrada con estado de ánimo nulo")
    void executeShouldReturnEntryWithNullMood() {
        // --- arrange ---
        givenRequestForUser(USER_ID);
        givenRepositoryReturnsEntryWithoutMood();

        // --- act ---
        ListJournalEntriesResponse result = list();

        // --- assert ---
        thenSingleEntryHasNullMood(result);
    }

    @Test
    @DisplayName("Propaga la excepción lanzada por el repositorio")
    void executeShouldPropagateExceptionFromRepository() {
        // --- arrange ---
        givenRequestForUser(USER_ID);
        givenRepositoryFails();

        // --- assert ---
        thenListThrowsRuntimeExceptionWithRepositoryError();
    }

    // --- arrange ---

    private void givenRequestForUser(Long userId) {
        request = new ListJournalEntriesRequest(userId);
    }

    private void givenRepositoryReturnsTwoEntries() {
        List<JournalEntry> entries = List.of(
                buildEntry(2L, "Segunda entrada", Mood.CALM),
                buildEntry(1L, "Primera entrada", Mood.HAPPY));
        when(journalEntryRepository.findAllByUserId(request.userId())).thenReturn(entries);
    }

    private void givenRepositoryReturnsNoEntries() {
        when(journalEntryRepository.findAllByUserId(request.userId())).thenReturn(List.of());
    }

    private void givenRepositoryReturnsEntryWithoutMood() {
        when(journalEntryRepository.findAllByUserId(request.userId()))
                .thenReturn(List.of(buildEntry(3L, "Sin estado de ánimo", null)));
    }

    private void givenRepositoryFails() {
        when(journalEntryRepository.findAllByUserId(request.userId()))
                .thenThrow(new RuntimeException(REPOSITORY_ERROR));
    }

    private JournalEntry buildEntry(Long id, String content, Mood mood) {
        return JournalEntry.builder()
                .id(id)
                .userId(request.userId())
                .journalId(JOURNAL_ID)
                .content(content)
                .mood(mood)
                .createdAt(CREATED_AT)
                .build();
    }

    // --- act ---

    private ListJournalEntriesResponse list() {
        return listJournalEntriesUseCase.execute(request);
    }

    // --- assert ---

    private void thenResponseHasEntriesInOrder(ListJournalEntriesResponse result, Long firstId, Long secondId) {
        assertThat(result.entries()).hasSize(2);
        assertThat(result.entries().get(0).id()).isEqualTo(firstId);
        assertThat(result.entries().get(1).id()).isEqualTo(secondId);
    }

    private void thenResponseIsEmpty(ListJournalEntriesResponse result) {
        assertThat(result.entries()).isEmpty();
    }

    private void thenSingleEntryHasNullMood(ListJournalEntriesResponse result) {
        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().get(0).mood()).isNull();
    }

    private void thenRepositoryQueriedForUser(Long userId) {
        verify(journalEntryRepository).findAllByUserId(userId);
    }

    private void thenListThrowsRuntimeExceptionWithRepositoryError() {
        assertThatThrownBy(this::list)
                .isInstanceOf(RuntimeException.class)
                .hasMessage(REPOSITORY_ERROR);
    }
}
