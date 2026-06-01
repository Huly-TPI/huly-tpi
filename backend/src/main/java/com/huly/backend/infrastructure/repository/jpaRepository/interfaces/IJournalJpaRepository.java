package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.JournalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IJournalJpaRepository extends JpaRepository<JournalEntity, Long> {
    Optional<JournalEntity> findFirstByAppUser_Id(Long userId);
}
