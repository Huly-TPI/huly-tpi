package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.mandala.AvailableMandala;
import com.huly.backend.domain.useCase.mandala.ListAvailableMandalasUseCase;
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

    @GetMapping
    public ResponseEntity<List<MandalaResponse>> getAvailableMandalas(
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserId(principal);
        List<MandalaResponse> response = listAvailableMandalasUseCase.execute(userId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
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
