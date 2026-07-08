package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.journal.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.JournalEntriesEntity;
import com.huly.backend.infrastructure.repository.entity.JournalEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IJournalEntryJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IJournalJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JournalEntryRepositoryImplTest {

    @Mock private IJournalJpaRepository journalJpaRepository;
    @Mock private IJournalEntryJpaRepository journalEntryJpaRepository;
    @Mock private AppUserRepository appUserRepository;

    @InjectMocks private JournalEntryRepositoryImpl repositoryImpl;

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Crea un journal nuevo cuando el usuario no tiene uno")
    void saveShouldCreateNewJournalWhenNoneExistsForUser() {
        AppUserEntity user = user(1L);
        JournalEntity newJournal = journal(10L, user);
        givenReferencedUser(1L, user);
        givenNoJournalForUser(1L);
        givenJournalSaved(newJournal);
        givenEntrySaved(entry(100L, newJournal, "Hoy fue un buen día", Mood.HAPPY));

        save(1L, "Hoy fue un buen día", Mood.HAPPY);

        thenNewJournalWasSaved();
    }

    @Test
    @DisplayName("Usa el journal existente cuando el usuario ya tiene uno")
    void saveShouldUseExistingJournalWhenOneAlreadyExistsForUser() {
        AppUserEntity user = user(1L);
        JournalEntity existingJournal = journal(10L, user);
        givenReferencedUser(1L, user);
        givenExistingJournal(1L, existingJournal);
        givenEntrySaved(entry(100L, existingJournal, "Segunda entrada", Mood.CALM));

        save(1L, "Segunda entrada", Mood.CALM);

        thenNoNewJournalWasSaved();
        thenEntryWasSaved();
    }

    @Test
    @DisplayName("Devuelve el dominio con los campos correctos")
    void saveShouldReturnDomainWithCorrectFields() {
        AppUserEntity user = user(1L);
        JournalEntity journal = journal(10L, user);
        givenReferencedUser(1L, user);
        givenExistingJournal(1L, journal);
        givenEntrySaved(entry(100L, journal, "Contenido de prueba", Mood.NEUTRAL));

        JournalEntry result = save(1L, "Contenido de prueba", Mood.NEUTRAL);

        thenDomainFieldsMatch(result);
    }

    @Test
    @DisplayName("Persiste la entrada con el contenido y mood correctos")
    void saveShouldPersistEntryWithCorrectContentAndMood() {
        AppUserEntity user = user(1L);
        JournalEntity journal = journal(10L, user);
        givenReferencedUser(1L, user);
        givenExistingJournal(1L, journal);
        givenEntrySaved(entry(100L, journal, "Me sentí ansioso", Mood.ANXIOUS));

        save(1L, "Me sentí ansioso", Mood.ANXIOUS);

        thenPersistedEntryMatches(journal);
    }

    @Test
    @DisplayName("Permite mood nulo al guardar")
    void saveShouldAllowNullMood() {
        AppUserEntity user = user(1L);
        JournalEntity journal = journal(10L, user);
        givenReferencedUser(1L, user);
        givenExistingJournal(1L, journal);
        givenEntrySaved(entry(100L, journal, "Sin mood", null));

        JournalEntry result = save(1L, "Sin mood", null);

        thenMoodIsNull(result);
    }

    // ── findAllByUserId ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Devuelve las entradas de dominio mapeadas")
    void findAllByUserIdShouldReturnMappedDomainEntries() {
        JournalEntity journal = journal(2L, user(1L));
        givenEntriesForUser(1L, List.of(
                entry(10L, journal, "Segunda entrada", Mood.CALM),
                entry(9L, journal, "Primera entrada", Mood.HAPPY)));

        List<JournalEntry> result = findAll(1L);

        thenMappedEntriesMatch(result, 1L);
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando el usuario no tiene entradas")
    void findAllByUserIdShouldReturnEmptyListWhenUserHasNoEntries() {
        givenEntriesForUser(99L, List.of());

        List<JournalEntry> result = findAll(99L);

        thenEmpty(result);
        thenEntriesQueriedFor(99L);
    }

    @Test
    @DisplayName("Mapea correctamente el mood nulo")
    void findAllByUserIdShouldMapNullMoodCorrectly() {
        JournalEntity journal = journal(1L, user(1L));
        givenEntriesForUser(1L, List.of(entry(5L, journal, "Sin mood", null)));

        List<JournalEntry> result = findAll(1L);

        thenSingleEntryHasNullMood(result);
    }

    @Test
    @DisplayName("Delega la búsqueda en el repositorio JPA")
    void findAllByUserIdShouldDelegateToJpaRepository() {
        givenEntriesForUser(3L, List.of());

        findAll(3L);

        thenEntriesQueriedFor(3L);
    }

    // --- arrange ---
    private void givenReferencedUser(Long userId, AppUserEntity user) {
        when(appUserRepository.getReferenceById(userId)).thenReturn(user);
    }

    private void givenNoJournalForUser(Long userId) {
        when(journalJpaRepository.findFirstByAppUser_Id(userId)).thenReturn(Optional.empty());
    }

    private void givenExistingJournal(Long userId, JournalEntity journal) {
        when(journalJpaRepository.findFirstByAppUser_Id(userId)).thenReturn(Optional.of(journal));
    }

    private void givenJournalSaved(JournalEntity journal) {
        when(journalJpaRepository.save(any(JournalEntity.class))).thenReturn(journal);
    }

    private void givenEntrySaved(JournalEntriesEntity entry) {
        when(journalEntryJpaRepository.save(any(JournalEntriesEntity.class))).thenReturn(entry);
    }

    private void givenEntriesForUser(Long userId, List<JournalEntriesEntity> entries) {
        when(journalEntryJpaRepository.findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId)).thenReturn(entries);
    }

    private AppUserEntity user(Long id) {
        return AppUserEntity.builder().id(id).build();
    }

    private JournalEntity journal(Long id, AppUserEntity user) {
        return JournalEntity.builder().id(id).appUser(user).build();
    }

    private JournalEntriesEntity entry(Long id, JournalEntity journal, String content, Mood mood) {
        return JournalEntriesEntity.builder()
                .id(id).journal(journal).content(content).mood(mood).createdAt(Instant.now()).build();
    }

    // --- act ---
    private JournalEntry save(Long userId, String content, Mood mood) {
        return repositoryImpl.save(userId, content, mood);
    }

    private List<JournalEntry> findAll(Long userId) {
        return repositoryImpl.findAllByUserId(userId);
    }

    // --- assert ---
    private void thenNewJournalWasSaved() {
        verify(journalJpaRepository).save(any(JournalEntity.class));
    }

    private void thenNoNewJournalWasSaved() {
        verify(journalJpaRepository, never()).save(any(JournalEntity.class));
    }

    private void thenEntryWasSaved() {
        verify(journalEntryJpaRepository).save(any(JournalEntriesEntity.class));
    }

    private void thenDomainFieldsMatch(JournalEntry result) {
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getJournalId()).isEqualTo(10L);
        assertThat(result.getContent()).isEqualTo("Contenido de prueba");
        assertThat(result.getMood()).isEqualTo(Mood.NEUTRAL);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    private void thenPersistedEntryMatches(JournalEntity journal) {
        ArgumentCaptor<JournalEntriesEntity> captor = ArgumentCaptor.forClass(JournalEntriesEntity.class);
        verify(journalEntryJpaRepository).save(captor.capture());
        JournalEntriesEntity captured = captor.getValue();
        assertThat(captured.getContent()).isEqualTo("Me sentí ansioso");
        assertThat(captured.getMood()).isEqualTo(Mood.ANXIOUS);
        assertThat(captured.getJournal()).isEqualTo(journal);
        assertThat(captured.getCreatedAt()).isNotNull();
    }

    private void thenMoodIsNull(JournalEntry result) {
        assertThat(result.getMood()).isNull();
    }

    private void thenMappedEntriesMatch(List<JournalEntry> result, Long userId) {
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getContent()).isEqualTo("Segunda entrada");
        assertThat(result.get(0).getMood()).isEqualTo(Mood.CALM);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        assertThat(result.get(0).getJournalId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(9L);
    }

    private void thenSingleEntryHasNullMood(List<JournalEntry> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMood()).isNull();
    }

    private void thenEmpty(List<JournalEntry> result) {
        assertThat(result).isEmpty();
    }

    private void thenEntriesQueriedFor(Long userId) {
        verify(journalEntryJpaRepository).findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId);
    }
}
