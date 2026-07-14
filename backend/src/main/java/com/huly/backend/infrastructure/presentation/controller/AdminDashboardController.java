package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.admin.dashboard.GetAdminDashboardUseCase;
import com.huly.backend.infrastructure.presentation.dto.admin.AdminDashboardResponse;
import com.huly.backend.infrastructure.presentation.mapper.AdminPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final GetAdminDashboardUseCase getAdminDashboardUseCase;
    private final AdminPresentationMapper adminPresentationMapper;

    @GetMapping
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(adminPresentationMapper.toAdminDashboardResponse(getAdminDashboardUseCase.execute()));
    }
}
