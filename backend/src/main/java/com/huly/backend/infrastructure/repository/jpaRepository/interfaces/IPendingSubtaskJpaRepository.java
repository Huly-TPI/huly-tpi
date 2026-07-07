package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.PendingSubtaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPendingSubtaskJpaRepository extends JpaRepository<PendingSubtaskEntity, Long> {
    Optional<PendingSubtaskEntity> findByIdAndTask_Id(Long id, Long taskId);
    int countByTask_Id(Long taskId);
}
