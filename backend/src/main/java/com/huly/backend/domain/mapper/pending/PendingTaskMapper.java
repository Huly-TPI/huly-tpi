package com.huly.backend.domain.mapper.pending;

import com.huly.backend.domain.dto.pending.PendingSubtaskResponse;
import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.model.pending.PendingSubtask;
import com.huly.backend.domain.model.pending.PendingTask;

import java.util.List;
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

    public List<PendingTaskResponse> toResponse(List<PendingTask> tasks, Set<Long> recommendedTaskIds) {
        return tasks.stream()
                .map(task -> toResponse(task, recommendedTaskIds))
                .toList();
    }

    private List<PendingSubtaskResponse> toSubtaskResponses(PendingTask task) {
        if (task.getSubtasks() == null) {
            return List.of();
        }
        return task.getSubtasks().stream()
                .map(this::toResponse)
                .toList();
    }

    public PendingSubtaskResponse toResponse(PendingSubtask subtask) {
        return new PendingSubtaskResponse(
                subtask.getId(),
                subtask.getTaskId(),
                subtask.getText(),
                subtask.isDone(),
                subtask.getPosition()
        );
    }
}
