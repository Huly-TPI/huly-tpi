package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.model.enums.PendingCategory;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingSubtask;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.PendingSubtaskEntity;
import com.huly.backend.infrastructure.repository.entity.PendingTaskEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPendingTaskJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PendingTaskRepositoryImpl implements PendingTaskRepository {

    private final IPendingTaskJpaRepository jpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public PendingTask create(Long userId, String title, String description, LocalDate dueDate,
                                 EstimatedDuration estimatedDuration, PendingCategory category,
                                 List<String> initialSubtaskTexts) {
        AppUserEntity user = appUserRepository.getReferenceById(userId);
        Instant now = Instant.now();

        PendingTaskEntity entity = PendingTaskEntity.builder()
                .user(user)
                .title(title)
                .description(description)
                .dueDate(dueDate)
                .estimatedDuration(estimatedDuration)
                .category(category)
                .status(PendingStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        List<PendingSubtaskEntity> subtaskEntities = new ArrayList<>();
        if (initialSubtaskTexts != null) {
            int position = 0;
            for (String text : initialSubtaskTexts) {
                subtaskEntities.add(PendingSubtaskEntity.builder()
                        .task(entity)
                        .text(text)
                        .done(false)
                        .position(position++)
                        .createdAt(now)
                        .build());
            }
        }
        entity.setSubtasks(subtaskEntities);

        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<PendingTask> findByIdAndUserId(Long id, Long userId) {
        return jpaRepository.findByIdAndUser_Id(id, userId).map(this::toDomain);
    }

    @Override
    public List<PendingTask> findAllByUserId(Long userId, PendingStatus statusFilter) {
        List<PendingTaskEntity> entities = statusFilter == null
                ? jpaRepository.findAllByUser_IdOrderByCreatedAtDesc(userId)
                : jpaRepository.findAllByUser_IdAndStatusOrderByCreatedAtDesc(userId, statusFilter);
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    public List<PendingTask> findPendingByUserId(Long userId) {
        return jpaRepository.findAllByUser_IdAndStatus(userId, PendingStatus.PENDING).stream()
                .map(this::toDomainWithoutSubtasks)
                .toList();
    }

    @Override
    public void delete(Long id, Long userId) {
        PendingTaskEntity entity = requireOwned(id, userId);
        jpaRepository.delete(entity);
    }

    @Override
    public PendingTask updateFields(Long id, String title, String description, LocalDate dueDate,
                                       EstimatedDuration estimatedDuration, PendingCategory category) {
        PendingTaskEntity entity = requireById(id);
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setDueDate(dueDate);
        entity.setEstimatedDuration(estimatedDuration);
        entity.setCategory(category);
        entity.setUpdatedAt(Instant.now());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public PendingTask updateMentalLoad(Long id, double score, MentalLoadBucket bucket) {
        PendingTaskEntity entity = requireById(id);
        entity.setMentalLoadScore(score);
        entity.setMentalLoadBucket(bucket);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public PendingTask markCompleted(Long id, Instant completedAt) {
        PendingTaskEntity entity = requireById(id);
        entity.setStatus(PendingStatus.COMPLETED);
        entity.setCompletedAt(completedAt);
        entity.setUpdatedAt(completedAt);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public PendingTask updatePosition(Long id, double positionX, double positionY, Double assignedRotationIfFirstPin, Instant now) {
        PendingTaskEntity entity = requireById(id);
        boolean firstPin = entity.getPinnedAt() == null;
        entity.setPositionX(positionX);
        entity.setPositionY(positionY);
        if (firstPin) {
            entity.setRotationDeg(assignedRotationIfFirstPin);
            entity.setPinnedAt(now);
        }
        entity.setUpdatedAt(now);
        return toDomain(jpaRepository.save(entity));
    }

    private PendingTaskEntity requireById(Long id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pending", "id", id));
    }

    private PendingTaskEntity requireOwned(Long id, Long userId) {
        return jpaRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Pending", "id", id));
    }

    private PendingTask toDomain(PendingTaskEntity entity) {
        PendingTask task = toDomainWithoutSubtasks(entity);
        List<PendingSubtask> subtasks = entity.getSubtasks() == null
                ? List.of()
                : entity.getSubtasks().stream()
                        .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                        .map(this::toDomainSubtask)
                        .toList();
        task.setSubtasks(new ArrayList<>(subtasks));
        return task;
    }

    private PendingTask toDomainWithoutSubtasks(PendingTaskEntity entity) {
        return PendingTask.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .dueDate(entity.getDueDate())
                .estimatedDuration(entity.getEstimatedDuration())
                .category(entity.getCategory())
                .status(entity.getStatus())
                .mentalLoadScore(entity.getMentalLoadScore())
                .mentalLoadBucket(entity.getMentalLoadBucket())
                .positionX(entity.getPositionX())
                .positionY(entity.getPositionY())
                .rotationDeg(entity.getRotationDeg())
                .pinnedAt(entity.getPinnedAt())
                .subtasks(new ArrayList<>())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    private PendingSubtask toDomainSubtask(PendingSubtaskEntity entity) {
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
