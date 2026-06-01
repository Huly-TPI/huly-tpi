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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JournalEntryRepositoryImplTest {

    @Mock
    private IJournalJpaRepository journalJpaRepository;

    @Mock
    private IJournalEntryJpaRepository journalEntryJpaRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private JournalEntryRepositoryImpl journalEntryRepositoryImpl;

    private AppUserEntity buildUser(Long id) {
        return AppUserEntity.builder().id(id).build();
    }

    private JournalEntity buildJournal(Long id, AppUserEntity user) {
        return JournalEntity.builder().id(id).appUser(user).build();
    }

    private JournalEntriesEntity buildEntryEntity(Long id, JournalEntity journal, String content, Mood mood) {
        return JournalEntriesEntity.builder()
                .id(id).journal(journal).content(content).mood(mood).createdAt(Instant.now())
                .build();
    }

    @Test
    void findAllByUserId_shouldReturnMappedDomainEntries() {
        Long userId = 1L;
        AppUserEntity user = buildUser(userId);
        JournalEntity journal = buildJournal(2L, user);

        List<JournalEntriesEntity> entities = List.of(
                buildEntryEntity(10L, journal, "Segunda entrada", Mood.CALM),
                buildEntryEntity(9L, journal, "Primera entrada", Mood.HAPPY)
        );

        when(journalEntryJpaRepository.findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId))
                .thenReturn(entities);

        List<JournalEntry> result = journalEntryRepositoryImpl.findAllByUserId(userId);

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

        List<JournalEntry> result = journalEntryRepositoryImpl.findAllByUserId(userId);

        assertThat(result).isEmpty();
        verify(journalEntryJpaRepository).findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId);
    }

    @Test
    void findAllByUserId_shouldMapNullMoodCorrectly() {
        Long userId = 1L;
        AppUserEntity user = buildUser(userId);
        JournalEntity journal = buildJournal(1L, user);
        JournalEntriesEntity entityWithoutMood = buildEntryEntity(5L, journal, "Sin mood", null);

        when(journalEntryJpaRepository.findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(entityWithoutMood));

        List<JournalEntry> result = journalEntryRepositoryImpl.findAllByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMood()).isNull();
    }

    @Test
    void findAllByUserId_shouldDelegateToJpaRepository() {
        Long userId = 3L;

        when(journalEntryJpaRepository.findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        journalEntryRepositoryImpl.findAllByUserId(userId);

        verify(journalEntryJpaRepository).findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId);
    }
}
