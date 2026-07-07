package com.huly.backend.domain.dto.pending;

public record PendingSubtaskResponse(Long id, Long taskId, String text, boolean done, int position) {}
