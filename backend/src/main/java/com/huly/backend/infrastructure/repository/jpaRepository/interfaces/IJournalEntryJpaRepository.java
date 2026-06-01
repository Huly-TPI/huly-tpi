package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.JournalEntriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IJournalEntryJpaRepository extends JpaRepository<JournalEntriesEntity, Long> {
    List<JournalEntriesEntity> findAllByJournal_AppUser_IdOrderByCreatedAtDesc(Long userId);
}
