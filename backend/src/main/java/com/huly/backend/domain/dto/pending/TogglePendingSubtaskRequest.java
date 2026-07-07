package com.huly.backend.domain.dto.pending;

public record TogglePendingSubtaskRequest(Long taskId, Long subtaskId, Long userId) {}
