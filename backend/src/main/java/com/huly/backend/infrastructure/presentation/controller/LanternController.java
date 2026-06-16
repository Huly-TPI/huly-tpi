package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.LanternThought;
import com.huly.backend.domain.model.enums.LanternStatus;
import com.huly.backend.domain.useCase.lantern.CreateLanternThoughtUseCase;
import com.huly.backend.domain.useCase.lantern.ListLanternThoughtsUseCase;
import com.huly.backend.domain.useCase.lantern.MarkWorkedOnUseCase;
import com.huly.backend.domain.useCase.lantern.UpdateLanternStatusUseCase;
import com.huly.backend.infrastructure.presentation.dto.lantern.LanternThoughtRequest;
import com.huly.backend.infrastructure.presentation.dto.lantern.LanternThoughtResponse;
import com.huly.backend.infrastructure.presentation.dto.lantern.UpdateLanternStatusRequest;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lanterns")
@RequiredArgsConstructor
public class LanternController {

    private final CreateLanternThoughtUseCase createUseCase;
    private final ListLanternThoughtsUseCase listUseCase;
    private final UpdateLanternStatusUseCase updateStatusUseCase;
    private final MarkWorkedOnUseCase markWorkedOnUseCase;

    @GetMapping
    public ResponseEntity<List<LanternThoughtResponse>> list(@AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(listUseCase.execute(userId).stream().map(this::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<LanternThoughtResponse> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody LanternThoughtRequest request) {
        Long userId = getUserId(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(createUseCase.execute(userId, request.text())));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateLanternStatusRequest request) {
        Long userId = getUserId(principal);
        LanternStatus newStatus;
        try {
            newStatus = LanternStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + request.status());
        }
        try {
            updateStatusUseCase.execute(id, userId, newStatus);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/worked-on")
    public ResponseEntity<Void> markWorkedOn(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        Long userId = getUserId(principal);
        markWorkedOnUseCase.execute(id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) throw new UnauthorizedException("Not authenticated");
        return Long.parseLong(principal.getUsername());
    }

    private LanternThoughtResponse toResponse(LanternThought thought) {
        return new LanternThoughtResponse(thought.getId(), thought.getText(), thought.isWorkedOn(), thought.getCreatedAt());
    }
}
