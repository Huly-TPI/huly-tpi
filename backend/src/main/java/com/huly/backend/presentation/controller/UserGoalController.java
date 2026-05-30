package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.useCase.userGoal.AddUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.DeleteUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.GetUserGoalsByUserUseCase;
import com.huly.backend.domain.useCase.userGoal.UpdateUserGoalUseCase;
import com.huly.backend.presentation.dto.userGoal.UserGoalListResponse;
import com.huly.backend.presentation.dto.userGoal.UserGoalPageResponse;
import com.huly.backend.presentation.dto.userGoal.UserGoalRequest;
import com.huly.backend.presentation.dto.userGoal.UserGoalResponse;
import com.huly.backend.presentation.dto.userGoal.UserGoalUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-goals")
@RequiredArgsConstructor
public class UserGoalController {

    private final AddUserGoalUseCase addUserGoalUseCase;
    private final GetUserGoalsByUserUseCase getUserGoalsByUserUseCase;
    private final DeleteUserGoalUseCase deleteUserGoalUseCase;
    private final UpdateUserGoalUseCase updateUserGoalUseCase;

    @PostMapping
    public ResponseEntity<UserGoalResponse> add(@Valid @RequestBody UserGoalRequest request) {
        UserGoal created = addUserGoalUseCase.execute(
                request.userId(),
                request.title(),
                request.description(),
                request.activityId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserGoalListResponse> listByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
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
