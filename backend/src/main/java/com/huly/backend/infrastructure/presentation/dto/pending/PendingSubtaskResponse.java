package com.huly.backend.infrastructure.presentation.dto.pending;

public record PendingSubtaskResponse(Long id, Long taskId, String text, boolean done, int position) {}
