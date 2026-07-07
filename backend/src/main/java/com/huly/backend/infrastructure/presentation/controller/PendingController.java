package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.pendingRecommendation.GetDailyRecommendationRequest;
import com.huly.backend.domain.useCase.pending.AddPendingSubtaskUseCase;
import com.huly.backend.domain.useCase.pending.CompletePendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.CreatePendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.DeletePendingSubtaskUseCase;
import com.huly.backend.domain.useCase.pending.DeletePendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.GetPendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.ListPendingTasksUseCase;
import com.huly.backend.domain.useCase.pending.TogglePendingSubtaskUseCase;
import com.huly.backend.domain.useCase.pending.UpdatePendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.UpdatePendingPositionUseCase;
import com.huly.backend.domain.useCase.pendingRecommendation.GetDailyRecommendationUseCase;
import com.huly.backend.domain.useCase.pendingRecommendation.RespondToRecommendationUseCase;
import com.huly.backend.infrastructure.presentation.dto.pending.AddSubtaskRequest;
import com.huly.backend.infrastructure.presentation.dto.pending.PendingRecommendationResponse;
import com.huly.backend.infrastructure.presentation.dto.pending.PendingTaskResponse;
import com.huly.backend.infrastructure.presentation.dto.pending.RespondToRecommendationRequest;
import com.huly.backend.infrastructure.presentation.dto.pending.UpdatePositionRequest;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.mapper.pending.PendingRecommendationPresentationMapper;
import com.huly.backend.infrastructure.presentation.mapper.pending.PendingPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pending")
@RequiredArgsConstructor
public class PendingController {

    private final CreatePendingTaskUseCase createPendingTaskUseCase;
    private final ListPendingTasksUseCase listPendingTasksUseCase;
    private final GetPendingTaskUseCase getPendingTaskUseCase;
    private final UpdatePendingTaskUseCase updatePendingTaskUseCase;
    private final DeletePendingTaskUseCase deletePendingTaskUseCase;
    private final CompletePendingTaskUseCase completePendingTaskUseCase;
    private final AddPendingSubtaskUseCase addPendingSubtaskUseCase;
    private final TogglePendingSubtaskUseCase togglePendingSubtaskUseCase;
    private final DeletePendingSubtaskUseCase deletePendingSubtaskUseCase;
    private final UpdatePendingPositionUseCase updatePendingPositionUseCase;
    private final GetDailyRecommendationUseCase getDailyRecommendationUseCase;
    private final RespondToRecommendationUseCase respondToRecommendationUseCase;
    private final PendingPresentationMapper mapper;
    private final PendingRecommendationPresentationMapper recommendationMapper;

    @PostMapping
    public ResponseEntity<PendingTaskResponse> create(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid com.huly.backend.infrastructure.presentation.dto.pending.CreatePendingTaskRequest request) {
        Long userId = getUserId(principal);
        var response = createPendingTaskUseCase.execute(mapper.toCreateRequest(userId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toTaskResponse(response));
    }

    @GetMapping
    public ResponseEntity<List<PendingTaskResponse>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String status) {
        Long userId = getUserId(principal);
        var response = listPendingTasksUseCase.execute(mapper.toListRequest(userId, status));
        return ResponseEntity.ok(mapper.toTaskResponses(response.tasks()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PendingTaskResponse> get(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        Long userId = getUserId(principal);
        var response = getPendingTaskUseCase.execute(mapper.toGetRequest(id, userId));
        return ResponseEntity.ok(mapper.toTaskResponse(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @RequestBody @Valid com.huly.backend.infrastructure.presentation.dto.pending.UpdatePendingTaskRequest request) {
        Long userId = getUserId(principal);
        updatePendingTaskUseCase.execute(mapper.toUpdateRequest(id, userId, request));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        Long userId = getUserId(principal);
        deletePendingTaskUseCase.execute(mapper.toDeleteRequest(id, userId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> complete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        Long userId = getUserId(principal);
        completePendingTaskUseCase.execute(mapper.toCompleteRequest(id, userId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/subtasks")
    public ResponseEntity<com.huly.backend.infrastructure.presentation.dto.pending.PendingSubtaskResponse> addSubtask(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @RequestBody @Valid AddSubtaskRequest request) {
        Long userId = getUserId(principal);
        var response = addPendingSubtaskUseCase.execute(mapper.toAddSubtaskRequest(id, userId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toSubtaskResponse(response));
    }

    @PatchMapping("/{id}/subtasks/{subtaskId}/toggle")
    public ResponseEntity<Void> toggleSubtask(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @PathVariable Long subtaskId) {
        Long userId = getUserId(principal);
        togglePendingSubtaskUseCase.execute(mapper.toToggleSubtaskRequest(id, subtaskId, userId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/subtasks/{subtaskId}")
    public ResponseEntity<Void> deleteSubtask(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @PathVariable Long subtaskId) {
        Long userId = getUserId(principal);
        deletePendingSubtaskUseCase.execute(mapper.toDeleteSubtaskRequest(id, subtaskId, userId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/position")
    public ResponseEntity<PendingTaskResponse> updatePosition(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @RequestBody @Valid UpdatePositionRequest request) {
        Long userId = getUserId(principal);
        var response = updatePendingPositionUseCase.execute(mapper.toPositionRequest(id, userId, request));
        return ResponseEntity.ok(mapper.toTaskResponse(response));
    }

    @GetMapping("/recommendation/today")
    public ResponseEntity<PendingRecommendationResponse> getTodayRecommendation(
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        var response = getDailyRecommendationUseCase.execute(new GetDailyRecommendationRequest(userId, LocalDate.now()));
        if (!response.applicable()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(recommendationMapper.toResponse(response));
    }

    @PostMapping("/recommendation/generate")
    public ResponseEntity<PendingRecommendationResponse> generateRecommendation(
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        var response = getDailyRecommendationUseCase.execute(new GetDailyRecommendationRequest(userId, LocalDate.now(), true));
        if (!response.applicable()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(recommendationMapper.toResponse(response));
    }

    @PostMapping("/recommendation/{id}/respond")
    public ResponseEntity<PendingRecommendationResponse> respondToRecommendation(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @RequestBody @Valid RespondToRecommendationRequest request) {
        Long userId = getUserId(principal);
        var response = respondToRecommendationUseCase.execute(mapper.toRespondRequest(id, userId, request));
        return ResponseEntity.ok(recommendationMapper.toResponse(response));
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }
}
