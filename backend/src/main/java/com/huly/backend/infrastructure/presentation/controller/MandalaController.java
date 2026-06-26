package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.mandala.ClearMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.GetMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.ListAvailableMandalasUseCase;
import com.huly.backend.domain.useCase.mandala.SaveMandalaProgressUseCase;
import com.huly.backend.infrastructure.presentation.dto.mandala.MandalaPageResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.mapper.mandala.MandalaPresentationMapper;
import lombok.RequiredArgsConstructor;
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
    private final MandalaPresentationMapper mandalaPresentationMapper;

    @GetMapping
    public ResponseEntity<MandalaPageResponse> getAvailableMandalas(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Long userId = currentUserId(principal);
        int pageNumber = Math.max(0, page);
        int pageSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        return ResponseEntity.ok(mandalaPresentationMapper.toPageResponse(
                listAvailableMandalasUseCase.execute(
                        mandalaPresentationMapper.toListRequest(userId, pageNumber, pageSize))));
    }

    @PutMapping(value = "/{id}/progress", consumes = "application/octet-stream")
    public ResponseEntity<Void> saveProgress(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable("id") String mandalaId,
            @RequestBody byte[] paintBlob) {
        Long userId = currentUserId(principal);
        saveMandalaProgressUseCase.execute(
                mandalaPresentationMapper.toSaveRequest(userId, mandalaId, paintBlob));
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{id}/progress", produces = "application/octet-stream")
    public ResponseEntity<byte[]> getProgress(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable("id") String mandalaId) {
        Long userId = currentUserId(principal);
        return mandalaPresentationMapper.toProgressResponse(
                getMandalaProgressUseCase.execute(
                        mandalaPresentationMapper.toGetRequest(userId, mandalaId)));
    }

    @DeleteMapping("/{id}/progress")
    public ResponseEntity<Void> clearProgress(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable("id") String mandalaId) {
        Long userId = currentUserId(principal);
        clearMandalaProgressUseCase.execute(
                mandalaPresentationMapper.toClearRequest(userId, mandalaId));
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }
}
