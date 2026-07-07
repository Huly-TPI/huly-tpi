package com.huly.backend.domain.repository.pending;

import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.model.enums.PendingCategory;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingTask;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PendingTaskRepository {

    PendingTask create(Long userId, String title, String description, LocalDate dueDate,
                          EstimatedDuration estimatedDuration, PendingCategory category,
                          List<String> initialSubtaskTexts);

    Optional<PendingTask> findByIdAndUserId(Long id, Long userId);

    List<PendingTask> findAllByUserId(Long userId, PendingStatus statusFilter);

    List<PendingTask> findPendingByUserId(Long userId);

    void delete(Long id, Long userId);

    PendingTask updateFields(Long id, String title, String description, LocalDate dueDate,
                                EstimatedDuration estimatedDuration, PendingCategory category);

    PendingTask updateMentalLoad(Long id, double score, MentalLoadBucket bucket);

    PendingTask markCompleted(Long id, Instant completedAt);

    PendingTask updatePosition(Long id, double positionX, double positionY, Double assignedRotationIfFirstPin, Instant now);
}
