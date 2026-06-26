package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.userGoal.AcceptChallengeResponse;
import com.huly.backend.domain.dto.userGoal.AddUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.CompleteUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.GetUserGoalsResponse;
import com.huly.backend.domain.dto.userGoal.UpdateUserGoalResponse;
import com.huly.backend.domain.useCase.userGoal.AcceptChallengeUseCase;
import com.huly.backend.domain.useCase.userGoal.AddUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.CompleteUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.DeleteUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.GetGoalImageUseCase;
import com.huly.backend.domain.useCase.userGoal.GetUserGoalsByUserUseCase;
import com.huly.backend.domain.useCase.userGoal.UpdateUserGoalUseCase;
import com.huly.backend.infrastructure.presentation.dto.userGoal.AcceptChallengeRequest;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalListResponse;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalRequest;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalResponse;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalUpdateRequest;
import com.huly.backend.infrastructure.presentation.dto.userPlant.GoalCompleteResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.mapper.userGoal.UserGoalPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

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
    private final GetGoalImageUseCase getGoalImageUseCase;
    private final UserGoalPresentationMapper mapper;

    @PostMapping("/accept")
    public ResponseEntity<UserGoalResponse> acceptChallenge(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AcceptChallengeRequest request) {
        Long userId = getUserId(principal);
        AcceptChallengeResponse response =
                acceptChallengeUseCase.execute(mapper.toAcceptChallengeRequest(userId, request));
        log.info("Challenge aceptado exitosamente. userGoalId='{}', userId='{}'", response.goal().id(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(response));
    }

    @PostMapping
    public ResponseEntity<UserGoalResponse> add(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UserGoalRequest request) {
        Long userId = getUserId(principal);
        AddUserGoalResponse response = addUserGoalUseCase.execute(mapper.toAddUserGoalRequest(userId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(response));
    }

    @GetMapping("/me")
    public ResponseEntity<UserGoalListResponse> listByUser(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Long userId = getUserId(principal);
        GetUserGoalsResponse response =
                getUserGoalsByUserUseCase.execute(mapper.toGetUserGoalsRequest(userId, page, size));
        return ResponseEntity.ok(mapper.toListResponse(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserGoalResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserGoalUpdateRequest request) {
        UpdateUserGoalResponse response = updateUserGoalUseCase.execute(mapper.toUpdateUserGoalRequest(id, request));
        return ResponseEntity.ok(mapper.toResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteUserGoalUseCase.execute(mapper.toDeleteUserGoalRequest(id));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GoalCompleteResponse> complete(
            @PathVariable Long id,
            @RequestParam(required = false) MultipartFile image) {
        CompleteUserGoalResponse response =
                completeUserGoalUseCase.execute(mapper.toCompleteUserGoalRequest(id), image);
        return ResponseEntity.ok(mapper.toGoalCompleteResponse(response));
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Path path = getGoalImageUseCase.execute(mapper.toGetGoalImageRequest(filename));
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "";
        MediaType mediaType = switch (ext) {
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            default -> MediaType.IMAGE_JPEG;
        };
        return ResponseEntity.ok().contentType(mediaType).body(resource);
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }
}
