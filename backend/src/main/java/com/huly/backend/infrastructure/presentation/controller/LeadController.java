package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.lead.RegisterLeadUseCase;
import com.huly.backend.infrastructure.presentation.dto.LeadRequestDto;
import com.huly.backend.infrastructure.presentation.dto.LeadResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/leads")
@PreAuthorize("permitAll()")
public class LeadController {

    private final RegisterLeadUseCase registerLeadUseCase;

    @PostMapping
    public ResponseEntity<LeadResponseDto> register(@Valid @RequestBody LeadRequestDto request) {
        registerLeadUseCase.execute(request.email(), request.nickname(), request.sourceAction());
        return ResponseEntity.status(HttpStatus.CREATED).body(new LeadResponseDto("Registro exitoso"));
    }
}
