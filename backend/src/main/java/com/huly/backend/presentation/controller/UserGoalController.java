package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.useCase.userGoal.AcceptChallengeUseCase;
import com.huly.backend.domain.useCase.userGoal.AddUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.CompleteUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.DeleteUserGoalUseCase;
import com.huly.backend.domain.useCase.userGoal.GetUserGoalsByUserUseCase;
import com.huly.backend.domain.useCase.userGoal.UpdateUserGoalUseCase;
import com.huly.backend.presentation.dto.userGoal.AcceptChallengeRequest;
import com.huly.backend.presentation.dto.userGoal.UserGoalListResponse;
import com.huly.backend.presentation.dto.userGoal.UserGoalPageResponse;
import com.huly.backend.presentation.dto.userGoal.UserGoalRequest;
import com.huly.backend.presentation.dto.userGoal.UserGoalResponse;
import com.huly.backend.presentation.dto.userGoal.UserGoalUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 * Controlador de metas/objetivos del usuario (UserGoals).
 * Las metas pueden crearse manualmente o desde un reto sugerido por el chatbot.
 *
 * Ciclo de vida de una meta:
 *  1. Se crea con estado PENDING (POST /accept desde chatbot o POST / manualmente)
 *  2. El usuario puede editarla (PUT /{id}) o eliminarla (DELETE /{id})
 *  3. Al completarla, cambia a estado COMPLETED (PATCH /{id}/complete)
 *
 * El listado separa pendientes y completadas en dos páginas independientes.
 */
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

    /**
     * Acepta un reto sugerido por el chatbot y lo convierte en una meta del usuario.
     * El usuario autenticado se resuelve desde el JWT (no se recibe userId en el body).
     * Registra un log de auditoría al completarse exitosamente.
     */
    @PostMapping("/accept")
    public ResponseEntity<UserGoalResponse> acceptChallenge(@Valid @RequestBody AcceptChallengeRequest request) {
        // El email del usuario autenticado proviene del JWT via SecurityContextHolder
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserGoal created = acceptChallengeUseCase.execute(
                email,
                request.title(),
                request.description(),
                request.activityId()
        );

        log.info("Challenge aceptado exitosamente. userGoalId='{}', email='{}'", created.getId(), email);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    /** Crea una meta manualmente (sin pasar por el chatbot). Requiere userId en el body. */
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

    /**
     * Lista las metas de un usuario separadas en pendientes y completadas, ambas paginadas.
     * El orden es descendente por fecha de creación (más recientes primero).
     */
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

    /** Actualiza el título, descripción o actividad asociada de una meta existente. */
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

    /** Elimina permanentemente una meta del usuario. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteUserGoalUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    /** Marca una meta como COMPLETED (cambio de estado irreversible). */
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
