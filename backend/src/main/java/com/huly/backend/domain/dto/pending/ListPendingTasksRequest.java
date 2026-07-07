package com.huly.backend.domain.dto.pending;

import com.huly.backend.domain.model.enums.PendingStatus;

public record ListPendingTasksRequest(Long userId, PendingStatus statusFilter) {}
