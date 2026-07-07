package com.huly.backend.infrastructure.presentation.mapper.pending;

import com.huly.backend.domain.dto.pending.AddPendingSubtaskRequest;
import com.huly.backend.domain.dto.pending.CompletePendingTaskRequest;
import com.huly.backend.domain.dto.pending.CreatePendingTaskRequest;
import com.huly.backend.domain.dto.pending.DeletePendingSubtaskRequest;
import com.huly.backend.domain.dto.pending.DeletePendingTaskRequest;
import com.huly.backend.domain.dto.pending.GetPendingTaskRequest;
import com.huly.backend.domain.dto.pending.ListPendingTasksRequest;
import com.huly.backend.domain.dto.pending.TogglePendingSubtaskRequest;
import com.huly.backend.domain.dto.pending.UpdatePendingTaskRequest;
import com.huly.backend.domain.dto.pending.UpdatePendingPositionRequest;
import com.huly.backend.domain.dto.pendingRecommendation.RespondToRecommendationRequest;
import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.PendingCategory;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import com.huly.backend.infrastructure.presentation.dto.pending.AddSubtaskRequest;
import com.huly.backend.infrastructure.presentation.dto.pending.PendingSubtaskResponse;
import com.huly.backend.infrastructure.presentation.dto.pending.PendingTaskResponse;
import com.huly.backend.infrastructure.presentation.dto.pending.UpdatePositionRequest;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PendingPresentationMapper {

    public CreatePendingTaskRequest toCreateRequest(Long userId, com.huly.backend.infrastructure.presentation.dto.pending.CreatePendingTaskRequest request) {
        return new CreatePendingTaskRequest(
                userId,
                request.title(),
                request.description(),
                request.dueDate(),
                parseDuration(request.estimatedDuration()),
                parseCategory(request.category()),
                request.subtasks()
        );
    }

    public UpdatePendingTaskRequest toUpdateRequest(Long id, Long userId, com.huly.backend.infrastructure.presentation.dto.pending.UpdatePendingTaskRequest request) {
        return new UpdatePendingTaskRequest(
                id,
                userId,
                request.title(),
                request.description(),
                request.dueDate(),
                parseDuration(request.estimatedDuration()),
                parseCategory(request.category())
        );
    }

    public ListPendingTasksRequest toListRequest(Long userId, String statusFilter) {
        return new ListPendingTasksRequest(userId, parseStatus(statusFilter));
    }

    public GetPendingTaskRequest toGetRequest(Long id, Long userId) {
        return new GetPendingTaskRequest(id, userId);
    }

    public DeletePendingTaskRequest toDeleteRequest(Long id, Long userId) {
        return new DeletePendingTaskRequest(id, userId);
    }

    public CompletePendingTaskRequest toCompleteRequest(Long id, Long userId) {
        return new CompletePendingTaskRequest(id, userId);
    }

    public AddPendingSubtaskRequest toAddSubtaskRequest(Long taskId, Long userId, AddSubtaskRequest request) {
        return new AddPendingSubtaskRequest(taskId, userId, request.text());
    }

    public TogglePendingSubtaskRequest toToggleSubtaskRequest(Long taskId, Long subtaskId, Long userId) {
        return new TogglePendingSubtaskRequest(taskId, subtaskId, userId);
    }

    public DeletePendingSubtaskRequest toDeleteSubtaskRequest(Long taskId, Long subtaskId, Long userId) {
        return new DeletePendingSubtaskRequest(taskId, subtaskId, userId);
    }

    public UpdatePendingPositionRequest toPositionRequest(Long id, Long userId, UpdatePositionRequest request) {
        return new UpdatePendingPositionRequest(id, userId, request.positionX(), request.positionY());
    }

    public RespondToRecommendationRequest toRespondRequest(Long recommendationId, Long userId, com.huly.backend.infrastructure.presentation.dto.pending.RespondToRecommendationRequest request) {
        return new RespondToRecommendationRequest(recommendationId, userId, parseDecision(request.decision()));
    }

    public PendingTaskResponse toTaskResponse(com.huly.backend.domain.dto.pending.PendingTaskResponse response) {
        return new PendingTaskResponse(
                response.id(),
                response.title(),
                response.description(),
                response.dueDate(),
                response.estimatedDuration() == null ? null : response.estimatedDuration().name(),
                response.category() == null ? null : response.category().name(),
                response.status().name(),
                toSubtaskResponses(response.subtasks()),
                response.positionX(),
                response.positionY(),
                response.rotationDeg(),
                response.pinnedAt(),
                response.recommended(),
                response.createdAt(),
                response.completedAt()
        );
    }

    public List<PendingTaskResponse> toTaskResponses(List<com.huly.backend.domain.dto.pending.PendingTaskResponse> responses) {
        return responses.stream().map(this::toTaskResponse).toList();
    }

    public PendingSubtaskResponse toSubtaskResponse(com.huly.backend.domain.dto.pending.PendingSubtaskResponse response) {
        return new PendingSubtaskResponse(response.id(), response.taskId(), response.text(), response.done(), response.position());
    }

    private List<PendingSubtaskResponse> toSubtaskResponses(List<com.huly.backend.domain.dto.pending.PendingSubtaskResponse> subtasks) {
        return subtasks.stream().map(this::toSubtaskResponse).toList();
    }

    private EstimatedDuration parseDuration(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return EstimatedDuration.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Duración estimada inválida: " + value);
        }
    }

    private PendingCategory parseCategory(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PendingCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Categoría inválida: " + value);
        }
    }

    private PendingStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PendingStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + value);
        }
    }

    private RecommendationResponseDecision parseDecision(String value) {
        try {
            RecommendationResponseDecision decision = RecommendationResponseDecision.valueOf(value.trim().toUpperCase());
            if (decision == RecommendationResponseDecision.PENDING) {
                throw new IllegalArgumentException(value);
            }
            return decision;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Decisión inválida: " + value);
        }
    }
}
