package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.repository.JournalEntryRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.JournalEntriesEntity;
import com.huly.backend.infrastructure.repository.entity.JournalEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IJournalEntryJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IJournalJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JournalEntryRepositoryImpl implements JournalEntryRepository {

    private final IJournalJpaRepository journalJpaRepository;
    private final IJournalEntryJpaRepository journalEntryJpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public JournalEntry save(Long userId, String content, Mood mood) {
        AppUserEntity user = appUserRepository.getReferenceById(userId);

        JournalEntity savedJournal = journalJpaRepository.findFirstByAppUser_Id(userId)
                .orElseGet(() -> journalJpaRepository.save(
                        JournalEntity.builder().appUser(user).build()
                ));

        JournalEntriesEntity entry = JournalEntriesEntity.builder()
                .journal(savedJournal)
                .content(content)
                .mood(mood)
                .createdAt(Instant.now())
                .build();
        JournalEntriesEntity savedEntry = journalEntryJpaRepository.save(entry);

        return toDomain(savedEntry, userId, savedJournal.getId());
    }

    @Override
    public List<JournalEntry> findAllByUserId(Long userId) {
        return journalEntryJpaRepository
                .findAllByJournal_AppUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(entity -> toDomain(entity, userId, entity.getJournal().getId()))
                .toList();
    }

    private JournalEntry toDomain(JournalEntriesEntity entity, Long userId, Long journalId) {
        return JournalEntry.builder()
                .id(entity.getId())
                .userId(userId)
                .journalId(journalId)
                .content(entity.getContent())
                .mood(entity.getMood())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
