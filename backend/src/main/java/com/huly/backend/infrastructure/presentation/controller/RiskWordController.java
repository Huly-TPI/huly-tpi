package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.riskWord.CreateRiskWordUseCase;
import com.huly.backend.domain.useCase.riskWord.DeleteRiskWordUseCase;
import com.huly.backend.domain.useCase.riskWord.ListRiskWordsUseCase;
import com.huly.backend.domain.useCase.riskWord.UpdateRiskWordUseCase;
import com.huly.backend.infrastructure.presentation.dto.riskWord.RiskWordPageResponse;
import com.huly.backend.infrastructure.presentation.dto.riskWord.RiskWordRequest;
import com.huly.backend.infrastructure.presentation.dto.riskWord.RiskWordResponse;
import com.huly.backend.infrastructure.presentation.mapper.riskWord.RiskWordPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/risk-words")
@PreAuthorize("hasRole('ADMIN')")
public class RiskWordController {

    private final CreateRiskWordUseCase createRiskWordUseCase;
    private final UpdateRiskWordUseCase updateRiskWordUseCase;
    private final DeleteRiskWordUseCase deleteRiskWordUseCase;
    private final ListRiskWordsUseCase listRiskWordsUseCase;
    private final RiskWordPresentationMapper riskWordPresentationMapper;

    @PostMapping
    public ResponseEntity<RiskWordResponse> create(@Valid @RequestBody RiskWordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                riskWordPresentationMapper.toResponse(
                        createRiskWordUseCase.execute(riskWordPresentationMapper.toCreateRequest(request))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RiskWordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RiskWordRequest request) {
        return ResponseEntity.ok(
                riskWordPresentationMapper.toResponse(
                        updateRiskWordUseCase.execute(riskWordPresentationMapper.toUpdateRequest(id, request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRiskWordUseCase.execute(riskWordPresentationMapper.toDeleteRequest(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<RiskWordPageResponse> list(
            @RequestParam(required = false) String word,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                riskWordPresentationMapper.toPageResponse(
                        listRiskWordsUseCase.execute(
                                riskWordPresentationMapper.toListRequest(word, active, severity, page, size))));
    }
}
