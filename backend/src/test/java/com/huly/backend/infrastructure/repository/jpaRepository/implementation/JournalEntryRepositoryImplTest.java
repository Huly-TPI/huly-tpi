package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.JournalEntriesEntity;
import com.huly.backend.infrastructure.repository.entity.JournalEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IJournalEntryJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IJournalJpaRepository;
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

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    void save_shouldCreateNewJournal_whenNoneExistsForUser() {
        AppUserEntity user = user(1L);
        JournalEntity newJournal = journal(10L, user);
        JournalEntriesEntity savedEntry = entry(100L, newJournal, "Hoy fue un buen día", Mood.HAPPY);

        when(appUserRepository.getReferenceById(1L)).thenReturn(user);
        when(journalJpaRepository.findFirstByAppUser_Id(1L)).thenReturn(Optional.empty());
        when(journalJpaRepository.save(any(JournalEntity.class))).thenReturn(newJournal);
        when(journalEntryJpaRepository.save(any(JournalEntriesEntity.class))).thenReturn(savedEntry);

        repositoryImpl.save(1L, "Hoy fue un buen día", Mood.HAPPY);

        verify(journalJpaRepository).save(any(JournalEntity.class));
    }

    @Test
    void save_shouldUseExistingJournal_whenOneAlreadyExistsForUser() {
        AppUserEntity user = user(1L);
        JournalEntity existingJournal = journal(10L, user);
        JournalEntriesEntity savedEntry = entry(100L, existingJournal, "Segunda entrada", Mood.CALM);

        when(appUserRepository.getReferenceById(1L)).thenReturn(user);
        when(journalJpaRepository.findFirstByAppUser_Id(1L)).thenReturn(Optional.of(existingJournal));
        when(journalEntryJpaRepository.save(any(JournalEntriesEntity.class))).thenReturn(savedEntry);

        repositoryImpl.save(1L, "Segunda entrada", Mood.CALM);

        verify(journalJpaRepository, never()).save(any(JournalEntity.class));
        verify(journalEntryJpaRepository).save(any(JournalEntriesEntity.class));
    }

    @Test
    void save_shouldReturnDomainWithCorrectFields() {
        AppUserEntity user = user(1L);
        JournalEntity journal = journal(10L, user);
        JournalEntriesEntity savedEntry = entry(100L, journal, "Contenido de prueba", Mood.NEUTRAL);

        when(appUserRepository.getReferenceById(1L)).thenReturn(user);
        when(journalJpaRepository.findFirstByAppUser_Id(1L)).thenReturn(Optional.of(journal));
        when(journalEntryJpaRepository.save(any())).thenReturn(savedEntry);

        JournalEntry result = repositoryImpl.save(1L, "Contenido de prueba", Mood.NEUTRAL);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getJournalId()).isEqualTo(10L);
        assertThat(result.getContent()).isEqualTo("Contenido de prueba");
        assertThat(result.getMood()).isEqualTo(Mood.NEUTRAL);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void save_shouldPersistEntryWithCorrectContentAndMood() {
        AppUserEntity user = user(1L);
        JournalEntity journal = journal(10L, user);
        JournalEntriesEntity savedEntry = entry(100L, journal, "Me sentí ansioso", Mood.ANXIOUS);

        when(appUserRepository.getReferenceById(1L)).thenReturn(user);
        when(journalJpaRepository.findFirstByAppUser_Id(1L)).thenReturn(Optional.of(journal));
        when(journalEntryJpaRepository.save(any(JournalEntriesEntity.class))).thenReturn(savedEntry);

        repositoryImpl.save(1L, "Me sentí ansioso", Mood.ANXIOUS);

        ArgumentCaptor<JournalEntriesEntity> captor = ArgumentCaptor.forClass(JournalEntriesEntity.class);
        verify(journalEntryJpaRepository).save(captor.capture());

        JournalEntriesEntity captured = captor.getValue();
        assertThat(captured.getContent()).isEqualTo("Me sentí ansioso");
        assertThat(captured.getMood()).isEqualTo(Mood.ANXIOUS);
        assertThat(captured.getJournal()).isEqualTo(journal);
        assertThat(captured.getCreatedAt()).isNotNull();
    }

    @Test
    void save_shouldAllowNullMood() {
        AppUserEntity user = user(1L);
        JournalEntity journal = journal(10L, user);
        JournalEntriesEntity savedEntry = entry(100L, journal, "Sin mood", null);

        when(appUserRepository.getReferenceById(1L)).thenReturn(user);
        when(journalJpaRepository.findFirstByAppUser_Id(1L)).thenReturn(Optional.of(journal));
        when(journalEntryJpaRepository.save(any())).thenReturn(savedEntry);

        JournalEntry result = repositoryImpl.save(1L, "Sin mood", null);

        assertThat(result.getMood()).isNull();
    }

    // ── findAllByUserId ────────────────────────────────────────────────────────

    @Test
    void findAllByUserId_shouldReturnMappedDomainEntries() {
        Long userId = 1L;
        AppUserEntity user = user(userId);
        JournalEntity journal = journal(2L, user);

        List<JournalEntriesEntity> entities = List.of(
                entry(10L, journal, "Segunda entrada", Mood.CALM),
                entry(9L, journal, "Primera entrada", Mood.HAPPY)
        );

        when(journalEntryJpaRepository.findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId))
                .thenReturn(entities);

        List<JournalEntry> result = repositoryImpl.findAllByUserId(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getContent()).isEqualTo("Segunda entrada");
        assertThat(result.get(0).getMood()).isEqualTo(Mood.CALM);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        assertThat(result.get(0).getJournalId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(9L);
    }

    @Test
    void findAllByUserId_shouldReturnEmptyList_whenUserHasNoEntries() {
        Long userId = 99L;

        when(journalEntryJpaRepository.findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        List<JournalEntry> result = repositoryImpl.findAllByUserId(userId);

        assertThat(result).isEmpty();
        verify(journalEntryJpaRepository).findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId);
    }

    @Test
    void findAllByUserId_shouldMapNullMoodCorrectly() {
        Long userId = 1L;
        AppUserEntity user = user(userId);
        JournalEntity journal = journal(1L, user);

        when(journalEntryJpaRepository.findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(entry(5L, journal, "Sin mood", null)));

        List<JournalEntry> result = repositoryImpl.findAllByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMood()).isNull();
    }

    @Test
    void findAllByUserId_shouldDelegateToJpaRepository() {
        Long userId = 3L;

        when(journalEntryJpaRepository.findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        repositoryImpl.findAllByUserId(userId);

        verify(journalEntryJpaRepository).findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId);
    }
}
