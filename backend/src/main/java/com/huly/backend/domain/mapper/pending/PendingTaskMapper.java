package com.huly.backend.domain.mapper.pending;

import com.huly.backend.domain.dto.pending.PendingSubtaskResponse;
import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.model.pending.PendingSubtask;
import com.huly.backend.domain.model.pending.PendingTask;

import java.util.Set;

public class PendingTaskMapper {

    public PendingTaskResponse toResponse(PendingTask task, boolean recommended) {
        return new PendingTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getEstimatedDuration(),
                task.getCategory(),
                task.getStatus(),
                toSubtaskResponses(task),
                task.getPositionX(),
                task.getPositionY(),
                task.getRotationDeg(),
                task.getPinnedAt(),
                recommended,
                task.getCreatedAt(),
                task.getCompletedAt()
        );
    }

    public PendingTaskResponse toResponse(PendingTask task, Set<Long> recommendedTaskIds) {
        return toResponse(task, recommendedTaskIds.contains(task.getId()));
    }

    private java.util.List<PendingSubtaskResponse> toSubtaskResponses(PendingTask task) {
        if (task.getSubtasks() == null) {
            return java.util.List.of();
        }
        return task.getSubtasks().stream()
                .map(this::toSubtaskResponse)
                .toList();
    }

    private PendingSubtaskResponse toSubtaskResponse(PendingSubtask subtask) {
        return new PendingSubtaskResponse(
                subtask.getId(),
                subtask.getTaskId(),
                subtask.getText(),
                subtask.isDone(),
                subtask.getPosition()
        );
    }
}
