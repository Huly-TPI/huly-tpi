package com.huly.backend.domain.dto.pending;

public record AddPendingSubtaskRequest(Long taskId, Long userId, String text) {}
