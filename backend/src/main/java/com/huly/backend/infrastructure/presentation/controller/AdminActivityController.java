package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.admin.activities.UpdateActivityConfigRequest;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.useCase.admin.activities.GetAdminActivitiesUseCase;
import com.huly.backend.domain.useCase.admin.activities.UpdateActivityConfigUseCase;
import com.huly.backend.domain.useCase.admin.activities.GetActivitiesKpiUseCase;
import com.huly.backend.domain.useCase.admin.activities.GetActivityPopularityUseCase;
import com.huly.backend.domain.useCase.admin.activities.GetActivityCorrelationUseCase;
import com.huly.backend.domain.useCase.admin.activities.GetActivityImpactUseCase;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminActivityResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminUpdateActivityConfigRequest;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminActivitiesKpiResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminActivityPopularityResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminActivityCorrelationResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminActivityImpactResponse;
import com.huly.backend.infrastructure.presentation.mapper.AdminPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final GetAdminActivitiesUseCase getAdminActivitiesUseCase;
    private final UpdateActivityConfigUseCase updateActivityConfigUseCase;
    private final GetActivitiesKpiUseCase getActivitiesKpiUseCase;
    private final GetActivityPopularityUseCase getActivityPopularityUseCase;
    private final GetActivityCorrelationUseCase getActivityCorrelationUseCase;
    private final GetActivityImpactUseCase getActivityImpactUseCase;
    private final AdminPresentationMapper adminPresentationMapper;

    @GetMapping
    public ResponseEntity<List<AdminActivityResponse>> getAdminActivities() {
        List<AdminActivityResponse> responses = getAdminActivitiesUseCase.execute().stream()
                .map(adminPresentationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminActivityResponse> updateActivityConfig(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateActivityConfigRequest request
    ) {
        UpdateActivityConfigRequest domainRequest = adminPresentationMapper.toDomainRequest(request);
        Activity updated = updateActivityConfigUseCase.execute(id, domainRequest);
        return ResponseEntity.ok(adminPresentationMapper.toResponse(updated));
    }

    @GetMapping("/kpis")
    public ResponseEntity<AdminActivitiesKpiResponse> getDashboardKpi(
            @RequestParam(value = "timeframe", required = false) String timeframe
    ) {
        Timeframe tf = Timeframe.fromString(timeframe);
        var stats = getActivitiesKpiUseCase.execute(tf);
        return ResponseEntity.ok(adminPresentationMapper.toKpiResponse(stats));
    }

    @GetMapping("/popularity")
    public ResponseEntity<List<AdminActivityPopularityResponse>> getActivityPopularity(
            @RequestParam(value = "timeframe", required = false) String timeframe
    ) {
        Timeframe tf = Timeframe.fromString(timeframe);
        List<AdminActivityPopularityResponse> list = getActivityPopularityUseCase.execute(tf).stream()
                .map(adminPresentationMapper::toPopularityResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/correlation")
    public ResponseEntity<List<AdminActivityCorrelationResponse>> getActivityCorrelation(
            @RequestParam(value = "timeframe", required = false) String timeframe
    ) {
        Timeframe tf = Timeframe.fromString(timeframe);
        List<AdminActivityCorrelationResponse> list = getActivityCorrelationUseCase.execute(tf).stream()
                .map(adminPresentationMapper::toCorrelationResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/impact")
    public ResponseEntity<List<AdminActivityImpactResponse>> getActivityImpact(
            @RequestParam(value = "timeframe", required = false) String timeframe
    ) {
        Timeframe tf = Timeframe.fromString(timeframe);
        List<AdminActivityImpactResponse> list = getActivityImpactUseCase.execute(tf).stream()
                .map(adminPresentationMapper::toImpactResponse)
                .toList();
        return ResponseEntity.ok(list);
    }
}
