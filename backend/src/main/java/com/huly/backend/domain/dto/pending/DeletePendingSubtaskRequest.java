package com.huly.backend.domain.dto.pending;

public record DeletePendingSubtaskRequest(Long taskId, Long subtaskId, Long userId) {}
