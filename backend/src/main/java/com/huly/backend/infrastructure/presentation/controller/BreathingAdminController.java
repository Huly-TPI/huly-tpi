package com.huly.backend.infrastructure.presentation.controller;
import com.huly.backend.domain.dto.breathingTechnique.CreateBreathingTechniqueRequest;
import com.huly.backend.domain.dto.breathingTechnique.UpdateBreathingTechniqueRequest;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.useCase.breathingTechnique.CreateBreathingTechniqueUseCase;
import com.huly.backend.domain.useCase.breathingTechnique.ListBreathingTechniquesUseCase;
import com.huly.backend.domain.useCase.breathingTechnique.SetBreathingTechniqueActiveUseCase;
import com.huly.backend.domain.useCase.breathingTechnique.UpdateBreathingTechniqueUseCase;
import com.huly.backend.infrastructure.presentation.dto.breathingTechniques.AdminBreathingTechniqueResponse;
import com.huly.backend.infrastructure.presentation.dto.breathingTechniques.BreathingTechniqueWebRequest;
import com.huly.backend.infrastructure.presentation.dto.payment.SetActiveWebRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/breathing-techniques")
@RequiredArgsConstructor
public class BreathingAdminController {

    private final ListBreathingTechniquesUseCase listBreathingTechniquesUseCase;
    private final CreateBreathingTechniqueUseCase createBreathingTechniqueUseCase;
    private final UpdateBreathingTechniqueUseCase updateBreathingTechniqueUseCase;
    private final SetBreathingTechniqueActiveUseCase setBreathingTechniqueActiveUseCase;

    @GetMapping
    public ResponseEntity<List<AdminBreathingTechniqueResponse>> list() {
        return ResponseEntity.ok(listBreathingTechniquesUseCase.execute().stream().map(this::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<AdminBreathingTechniqueResponse> create(@Valid @RequestBody BreathingTechniqueWebRequest req) {
        BreathingTechnique created = createBreathingTechniqueUseCase.execute(new CreateBreathingTechniqueRequest(
                req.name(), req.description(), req.inhaleSeconds(), req.holdSeconds(),
                req.exhaleSeconds(), req.roundsInterval(), req.rounds()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminBreathingTechniqueResponse> update(@PathVariable Long id,
            @Valid @RequestBody BreathingTechniqueWebRequest req) {
        BreathingTechnique updated = updateBreathingTechniqueUseCase.execute(new UpdateBreathingTechniqueRequest(
                id, req.name(), req.description(), req.inhaleSeconds(), req.holdSeconds(),
                req.exhaleSeconds(), req.roundsInterval(), req.rounds()));
        return ResponseEntity.ok(toResponse(updated));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<AdminBreathingTechniqueResponse> setActive(@PathVariable Long id,
            @RequestBody SetActiveWebRequest req) {
        return ResponseEntity.ok(toResponse(setBreathingTechniqueActiveUseCase.execute(id, req.active())));
    }

    private AdminBreathingTechniqueResponse toResponse(BreathingTechnique t) {
        return new AdminBreathingTechniqueResponse(t.getId(), t.getName(), t.getDescription(),
                t.getInhaleSeconds(), t.getHoldSeconds(), t.getExhaleSeconds(),
                t.getRoundsInterval(), t.getRounds(), t.isActive());
    }
}