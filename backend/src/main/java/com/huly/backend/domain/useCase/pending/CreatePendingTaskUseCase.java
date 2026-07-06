package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.CreatePendingTaskRequest;
import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import com.huly.backend.domain.service.pending.PendingMentalLoadRefreshService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreatePendingTaskUseCase {

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingMentalLoadRefreshService mentalLoadRefreshService;
    private final PendingTaskMapper mapper;

    public PendingTaskResponse execute(CreatePendingTaskRequest request) {
        PendingTask created = pendingTaskRepository.create(
                request.userId(),
                request.title(),
                request.description(),
                request.dueDate(),
                request.estimatedDuration(),
                request.category(),
                request.initialSubtaskTexts()
        );

        PendingTask withMentalLoad = mentalLoadRefreshService.refresh(created);
        return mapper.toResponse(withMentalLoad, false);
    }
}
