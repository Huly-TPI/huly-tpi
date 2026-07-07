package com.huly.backend.domain.repository.pending;

import com.huly.backend.domain.model.pending.PendingSubtask;

import java.util.Optional;

public interface PendingSubtaskRepository {
    PendingSubtask create(Long taskId, String text, int position);
    Optional<PendingSubtask> findByIdAndTaskId(Long subtaskId, Long taskId);
    PendingSubtask toggle(Long subtaskId);
    void delete(Long subtaskId);
    int countByTaskId(Long taskId);
}
