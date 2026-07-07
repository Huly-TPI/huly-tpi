package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.pending.PendingSubtask;
import com.huly.backend.domain.repository.pending.PendingSubtaskRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.PendingSubtaskEntity;
import com.huly.backend.infrastructure.repository.entity.PendingTaskEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPendingSubtaskJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPendingTaskJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PendingSubtaskRepositoryImpl implements PendingSubtaskRepository {

    private final IPendingSubtaskJpaRepository jpaRepository;
    private final IPendingTaskJpaRepository taskJpaRepository;

    @Override
    public PendingSubtask create(Long taskId, String text, int position) {
        PendingTaskEntity task = taskJpaRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Pending", "id", taskId));

        PendingSubtaskEntity entity = PendingSubtaskEntity.builder()
                .task(task)
                .text(text)
                .done(false)
                .position(position)
                .createdAt(Instant.now())
                .build();

        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<PendingSubtask> findByIdAndTaskId(Long subtaskId, Long taskId) {
        return jpaRepository.findByIdAndTask_Id(subtaskId, taskId).map(this::toDomain);
    }

    @Override
    public PendingSubtask toggle(Long subtaskId) {
        PendingSubtaskEntity entity = jpaRepository.findById(subtaskId)
                .orElseThrow(() -> new NotFoundException("Subtarea", "id", subtaskId));
        entity.setDone(!entity.isDone());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public void delete(Long subtaskId) {
        jpaRepository.deleteById(subtaskId);
    }

    @Override
    public int countByTaskId(Long taskId) {
        return jpaRepository.countByTask_Id(taskId);
    }

    private PendingSubtask toDomain(PendingSubtaskEntity entity) {
        return PendingSubtask.builder()
                .id(entity.getId())
                .taskId(entity.getTask().getId())
                .text(entity.getText())
                .done(entity.isDone())
                .position(entity.getPosition())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
