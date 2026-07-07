package com.huly.backend.domain.dto.pending;

import java.util.List;

public record ListPendingTasksResponse(List<PendingTaskResponse> tasks) {}
