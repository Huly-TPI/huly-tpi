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
        PendingTask created = createPendingTask(request);
        PendingTask withMentalLoad = refreshMentalLoad(created);
        return mapper.toResponse(withMentalLoad, false);
    }

    private PendingTask createPendingTask(CreatePendingTaskRequest request) {
        return pendingTaskRepository.create(
                request.userId(),
                request.title(),
                request.description(),
                request.dueDate(),
                request.estimatedDuration(),
                request.category(),
                request.initialSubtaskTexts()
        );
    }

    private PendingTask refreshMentalLoad(PendingTask task) {
        return mentalLoadRefreshService.refresh(task);
    }
}
