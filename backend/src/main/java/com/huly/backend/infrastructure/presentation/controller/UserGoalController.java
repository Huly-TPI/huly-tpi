package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.useCase.userGoal.AcceptChallengeUseCase;
import com.huly.backend.domain.useCase.userGoal.AddUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.CompleteUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.DeleteUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.GetUserGoalsByUserUseCase;
import com.huly.backend.domain.useCase.userGoal.UpdateUserGoalUseCase;
import com.huly.backend.infrastructure.presentation.dto.userGoal.AcceptChallengeRequest;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalListResponse;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalPageResponse;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalRequest;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalResponse;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user-goals")
@RequiredArgsConstructor
public class UserGoalController {

    private final AcceptChallengeUseCase acceptChallengeUseCase;
    private final AddUserGoalUseCase addUserGoalUseCase;
    private final GetUserGoalsByUserUseCase getUserGoalsByUserUseCase;
    private final DeleteUserGoalUseCase deleteUserGoalUseCase;
    private final UpdateUserGoalUseCase updateUserGoalUseCase;
    private final CompleteUserGoalUseCase completeUserGoalUseCase;

    @PostMapping("/accept")
    public ResponseEntity<UserGoalResponse> acceptChallenge(@Valid @RequestBody AcceptChallengeRequest request) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        UserGoal created = acceptChallengeUseCase.execute(
                userId,
                request.title(),
                request.description(),
                request.activityId()
        );

        log.info("Challenge aceptado exitosamente. userGoalId='{}', userId='{}'", created.getId(), userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PostMapping
    public ResponseEntity<UserGoalResponse> add(@Valid @RequestBody UserGoalRequest request) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        UserGoal created = addUserGoalUseCase.execute(
                userId,
                request.title(),
                request.description(),
                request.activityId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping("/me")
    public ResponseEntity<UserGoalListResponse> listByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserGoal> completados = getUserGoalsByUserUseCase.executeCompleted(userId, pageable);
        Page<UserGoal> pendientes = getUserGoalsByUserUseCase.executePending(userId, pageable);
        return ResponseEntity.ok(new UserGoalListResponse(toPageResponse(completados), toPageResponse(pendientes)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserGoalResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserGoalUpdateRequest request) {
        UserGoal updated = updateUserGoalUseCase.execute(
                id,
                request.title(),
                request.description(),
                request.activityId()
        );
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteUserGoalUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<UserGoalResponse> complete(@PathVariable Long id) {
        UserGoal completed = completeUserGoalUseCase.execute(id);
        return ResponseEntity.ok(toResponse(completed));
    }

    private UserGoalResponse toResponse(UserGoal userGoal) {
        return new UserGoalResponse(
                userGoal.getId(),
                userGoal.getUserId(),
                userGoal.getTitle(),
                userGoal.getDescription(),
                userGoal.getStatus().name(),
                userGoal.getCreatedAt(),
                userGoal.getActivityId()
        );
    }

    private UserGoalPageResponse toPageResponse(Page<UserGoal> page) {
        return new UserGoalPageResponse(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}