package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.lead.RegisterLeadResponse;
import com.huly.backend.domain.useCase.lead.RegisterLeadUseCase;
import com.huly.backend.infrastructure.presentation.dto.lead.LeadRequestDto;
import com.huly.backend.infrastructure.presentation.dto.lead.LeadResponseDto;
import com.huly.backend.infrastructure.presentation.mapper.lead.LeadPresentationMapper;
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
    private final LeadPresentationMapper leadPresentationMapper;

    @PostMapping
    public ResponseEntity<LeadResponseDto> register(@Valid @RequestBody LeadRequestDto request) {
        RegisterLeadResponse response = registerLeadUseCase.execute(leadPresentationMapper.toRegisterRequest(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(leadPresentationMapper.toLeadResponse(response));
    }
}
