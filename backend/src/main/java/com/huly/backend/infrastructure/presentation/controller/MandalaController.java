package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.mandala.AvailableMandala;
import com.huly.backend.domain.useCase.mandala.ClearMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.GetMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.ListAvailableMandalasUseCase;
import com.huly.backend.domain.useCase.mandala.SaveMandalaProgressUseCase;
import com.huly.backend.infrastructure.presentation.dto.mandala.MandalaResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mandalas")
@RequiredArgsConstructor
public class MandalaController {

    private final ListAvailableMandalasUseCase listAvailableMandalasUseCase;
    private final SaveMandalaProgressUseCase saveMandalaProgressUseCase;
    private final GetMandalaProgressUseCase getMandalaProgressUseCase;
    private final ClearMandalaProgressUseCase clearMandalaProgressUseCase;

    @GetMapping
    public ResponseEntity<List<MandalaResponse>> getAvailableMandalas(
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserId(principal);
        List<MandalaResponse> response = listAvailableMandalasUseCase.execute(userId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}/progress", consumes = "application/octet-stream")
    public ResponseEntity<Void> saveProgress(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable("id") String mandalaId,
            @RequestBody byte[] paintBlob) {
        Long userId = currentUserId(principal);
        saveMandalaProgressUseCase.execute(userId, mandalaId, paintBlob);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{id}/progress", produces = "application/octet-stream")
    public ResponseEntity<byte[]> getProgress(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable("id") String mandalaId) {
        Long userId = currentUserId(principal);
        return getMandalaProgressUseCase.execute(userId, mandalaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/progress")
    public ResponseEntity<Void> clearProgress(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable("id") String mandalaId) {
        Long userId = currentUserId(principal);
        clearMandalaProgressUseCase.execute(userId, mandalaId);
        return ResponseEntity.noContent().build();
    }

    private MandalaResponse toResponse(AvailableMandala availableMandala) {
        return new MandalaResponse(
                availableMandala.getMandala().getId(),
                availableMandala.getMandala().getTitle(),
                availableMandala.getMandala().getDescription(),
                availableMandala.getMandala().getAssetKey(),
                availableMandala.getMandala().getDisplayOrder(),
                availableMandala.getUnlockSource().name());
    }

    private Long currentUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }
}
