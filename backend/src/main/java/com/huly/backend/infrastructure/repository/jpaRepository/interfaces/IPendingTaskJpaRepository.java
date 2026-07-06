package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.infrastructure.repository.entity.PendingTaskEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IPendingTaskJpaRepository extends JpaRepository<PendingTaskEntity, Long> {

    @EntityGraph(attributePaths = "subtasks")
    Optional<PendingTaskEntity> findByIdAndUser_Id(Long id, Long userId);

    @EntityGraph(attributePaths = "subtasks")
    List<PendingTaskEntity> findAllByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, PendingStatus status);

    @EntityGraph(attributePaths = "subtasks")
    List<PendingTaskEntity> findAllByUser_IdOrderByCreatedAtDesc(Long userId);

    List<PendingTaskEntity> findAllByUser_IdAndStatus(Long userId, PendingStatus status);
}
