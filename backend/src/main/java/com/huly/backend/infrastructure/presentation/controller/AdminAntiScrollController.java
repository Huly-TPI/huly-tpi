package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.admin.antiscroll.GetAntiScrollDashboardUseCase;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.GetAntiScrollGlobalConfigUseCase;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.UpdateAntiScrollGlobalConfigRequest;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.UpdateAntiScrollGlobalConfigUseCase;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollDashboardResponse;
import com.huly.backend.infrastructure.presentation.mapper.AdminPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/antiscroll")
public class AdminAntiScrollController {

    private final GetAntiScrollDashboardUseCase getAntiScrollDashboardUseCase;
    private final GetAntiScrollGlobalConfigUseCase getAntiScrollGlobalConfigUseCase;
    private final UpdateAntiScrollGlobalConfigUseCase updateAntiScrollGlobalConfigUseCase;
    private final AdminPresentationMapper adminPresentationMapper;

    @GetMapping("/dashboard")
    public ResponseEntity<AntiScrollDashboardResponse> getAntiScrollDashboardStats() {
        return ResponseEntity.ok(adminPresentationMapper.toAntiScrollDashboardResponse(getAntiScrollDashboardUseCase.execute()));
    }

    @GetMapping("/config")
    public ResponseEntity<AntiScrollConfigResponse> getAntiScrollConfig() {
        var result = getAntiScrollGlobalConfigUseCase.execute();
        return ResponseEntity.ok(adminPresentationMapper.toAntiScrollConfigResponse(result));
    }

    @PostMapping("/config")
    public ResponseEntity<Void> updateAntiScrollConfig(@Valid @RequestBody AntiScrollConfigRequest request) {
        updateAntiScrollGlobalConfigUseCase.execute(new UpdateAntiScrollGlobalConfigRequest(
                request.getDefaultPauseIntervalMinutes(),
                request.getTermsAndConditions()
        ));
        return ResponseEntity.ok().build();
    }
}
