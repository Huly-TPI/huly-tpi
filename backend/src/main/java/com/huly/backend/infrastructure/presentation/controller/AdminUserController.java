package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.useCase.admin.GetAntiScrollDashboardUseCase;
import com.huly.backend.domain.useCase.admin.ListBackofficeUsersUseCase;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.GetAntiScrollConfigUseCase;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.UpdateAntiScrollConfigRequest;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.UpdateAntiScrollConfigUseCase;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesRequest;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesUseCase;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsRequest;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsUseCase;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsRequest;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsUseCase;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsRequest;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsUseCase;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollDashboardResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.BackofficeUserResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.UserActivitiesResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.UserAiDiagnosticsResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.UserAntiScrollResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.UserFinancialsResponse;
import com.huly.backend.infrastructure.presentation.mapper.AdminPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final ListBackofficeUsersUseCase listBackofficeUsersUseCase;
    private final GetAntiScrollDashboardUseCase getAntiScrollDashboardUseCase;
    private final GetAntiScrollConfigUseCase getAntiScrollConfigUseCase;
    private final UpdateAntiScrollConfigUseCase updateAntiScrollConfigUseCase;
    private final GetUserActivitiesUseCase getUserActivitiesUseCase;
    private final GetUserAiDiagnosticsUseCase getUserAiDiagnosticsUseCase;
    private final GetUserFinancialsUseCase getUserFinancialsUseCase;
    private final GetUserAntiScrollStatsUseCase getUserAntiScrollStatsUseCase;
    private final AdminPresentationMapper adminPresentationMapper;

    @GetMapping("/{id}/statistics/activities")
    public ResponseEntity<UserActivitiesResponse> getUserActivities(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "total") String timeframe) {
        var result = getUserActivitiesUseCase.execute(new GetUserActivitiesRequest(id, Timeframe.fromString(timeframe)));
        return ResponseEntity.ok(adminPresentationMapper.toUserActivitiesResponse(result));
    }

    @GetMapping("/{id}/statistics/ai")
    public ResponseEntity<UserAiDiagnosticsResponse> getUserAiDiagnostics(@PathVariable Long id) {
        var result = getUserAiDiagnosticsUseCase.execute(new GetUserAiDiagnosticsRequest(id));
        return ResponseEntity.ok(adminPresentationMapper.toUserAiDiagnosticsResponse(result));
    }

    @GetMapping("/{id}/statistics/finance")
    public ResponseEntity<UserFinancialsResponse> getUserFinancials(@PathVariable Long id) {
        var result = getUserFinancialsUseCase.execute(new GetUserFinancialsRequest(id));
        return ResponseEntity.ok(adminPresentationMapper.toUserFinancialsResponse(result));
    }

    @GetMapping("/{id}/statistics/antiscroll")
    public ResponseEntity<UserAntiScrollResponse> getUserAntiScrollStats(@PathVariable Long id) {
        var result = getUserAntiScrollStatsUseCase.execute(new GetUserAntiScrollStatsRequest(id));
        return ResponseEntity.ok(adminPresentationMapper.toUserAntiScrollResponse(result));
    }

    @GetMapping
    public ResponseEntity<List<BackofficeUserResponse>> getBackofficeUsers(@RequestParam(required = false) String search) {
        List<BackofficeUserResponse> responses = listBackofficeUsersUseCase.execute(search).stream()
                .map(adminPresentationMapper::toBackofficeUserResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/antiscroll/dashboard")
    public ResponseEntity<AntiScrollDashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(adminPresentationMapper.toAntiScrollDashboardResponse(getAntiScrollDashboardUseCase.execute()));
    }

    @GetMapping("/antiscroll/config")
    public ResponseEntity<AntiScrollConfigResponse> getAntiScrollConfig() {
        var result = getAntiScrollConfigUseCase.execute();
        return ResponseEntity.ok(adminPresentationMapper.toAntiScrollConfigResponse(result));
    }

    @PostMapping("/antiscroll/config")
    public ResponseEntity<Void> updateAntiScrollConfig(@Valid @RequestBody AntiScrollConfigRequest request) {
        updateAntiScrollConfigUseCase.execute(new UpdateAntiScrollConfigRequest(
                request.getDefaultPauseIntervalMinutes(),
                request.getTermsAndConditions()
        ));
        return ResponseEntity.ok().build();
    }
}
