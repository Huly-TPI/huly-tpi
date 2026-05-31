package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.JournalEntriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IJournalEntryJpaRepository extends JpaRepository<JournalEntriesEntity, Long> {
}
