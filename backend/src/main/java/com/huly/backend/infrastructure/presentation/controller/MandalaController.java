package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.mandala.AvailableMandala;
import com.huly.backend.domain.useCase.mandala.ClearMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.GetMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.ListAvailableMandalasUseCase;
import com.huly.backend.domain.useCase.mandala.SaveMandalaProgressUseCase;
import com.huly.backend.infrastructure.presentation.dto.mandala.MandalaPageResponse;
import com.huly.backend.infrastructure.presentation.dto.mandala.MandalaResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mandalas")
@RequiredArgsConstructor
public class MandalaController {

    private static final int DEFAULT_PAGE_SIZE = 6;
    private static final int MAX_PAGE_SIZE = 12;

    private final ListAvailableMandalasUseCase listAvailableMandalasUseCase;
    private final SaveMandalaProgressUseCase saveMandalaProgressUseCase;
    private final GetMandalaProgressUseCase getMandalaProgressUseCase;
    private final ClearMandalaProgressUseCase clearMandalaProgressUseCase;

    @GetMapping
    public ResponseEntity<MandalaPageResponse> getAvailableMandalas(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Long userId = currentUserId(principal);
        int pageNumber = Math.max(0, page);
        int pageSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, Sort.by("displayOrder").ascending());
        Page<AvailableMandala> response = listAvailableMandalasUseCase.execute(userId, pageable);
        return ResponseEntity.ok(toPageResponse(response));
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
                availableMandala.getUnlockSource() != null ? availableMandala.getUnlockSource().name() : null,
                availableMandala.getMandala().getAccessType().name(),
                availableMandala.isLocked());
    }

    private MandalaPageResponse toPageResponse(Page<AvailableMandala> page) {
        return new MandalaPageResponse(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private Long currentUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }
}
