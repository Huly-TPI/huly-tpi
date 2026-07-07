package com.huly.backend.domain.service.pending;

import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.port.pending.MentalLoadEstimate;
import com.huly.backend.domain.port.pending.MentalLoadEstimationInput;
import com.huly.backend.domain.port.pending.MentalLoadEstimationPort;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@RequiredArgsConstructor
public class PendingMentalLoadRefreshService {

    private final MentalLoadEstimationPort mentalLoadEstimationPort;
    private final PendingTaskRepository pendingTaskRepository;

    public PendingTask refresh(PendingTask task) {
        try {
            MentalLoadEstimationInput input = buildInput(task);
            MentalLoadEstimate estimate = mentalLoadEstimationPort.estimate(input);
            return pendingTaskRepository.updateMentalLoad(task.getId(), estimate.score(), estimate.bucket());
        } catch (Exception e) {
            log.warn("No se pudo estimar la carga mental de la tarea {}: {}", task.getId(), e.getMessage());
            return task;
        }
    }

    private MentalLoadEstimationInput buildInput(PendingTask task) {
        int subtaskCount = task.getSubtasks() == null ? 0 : task.getSubtasks().size();
        return new MentalLoadEstimationInput(
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                daysUntilDue(task.getDueDate()),
                task.getEstimatedDuration(),
                task.getCategory(),
                subtaskCount
        );
    }

    private Integer daysUntilDue(LocalDate dueDate) {
        if (dueDate == null) {
            return null;
        }
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }
}
