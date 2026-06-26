package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.extension.GetUserAntiScrollSettingsResponse;
import com.huly.backend.domain.useCase.extension.GetUserAntiScrollSettingsUseCase;
import com.huly.backend.domain.useCase.extension.SaveExtensionMetricsUseCase;
import com.huly.backend.domain.useCase.extension.SaveUserAntiScrollSettingsUseCase;
import com.huly.backend.infrastructure.presentation.dto.extension.ExtensionMetricRequest;
import com.huly.backend.infrastructure.presentation.dto.extension.AntiScrollSettingsRequest;
import com.huly.backend.infrastructure.presentation.dto.extension.AntiScrollSettingsResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.mapper.extension.ExtensionPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extension")
@RequiredArgsConstructor
public class ExtensionController {

    private final GetUserAntiScrollSettingsUseCase getUserAntiScrollSettingsUseCase;
    private final SaveUserAntiScrollSettingsUseCase saveUserAntiScrollSettingsUseCase;
    private final SaveExtensionMetricsUseCase saveExtensionMetricsUseCase;
    private final ExtensionPresentationMapper extensionPresentationMapper;

    @GetMapping("/settings")
    public ResponseEntity<AntiScrollSettingsResponse> getSettings(
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = getUserId(principal);
        GetUserAntiScrollSettingsResponse settings = getUserAntiScrollSettingsUseCase.execute(
                extensionPresentationMapper.toGetSettingsRequest(userId));
        return ResponseEntity.ok(extensionPresentationMapper.toSettingsResponse(settings));
    }

    @PostMapping("/settings")
    public ResponseEntity<Void> saveSettings(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AntiScrollSettingsRequest request
    ) {
        Long userId = getUserId(principal);
        saveUserAntiScrollSettingsUseCase.execute(
                extensionPresentationMapper.toSaveSettingsRequest(userId, request));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/metrics")
    public ResponseEntity<Void> saveMetrics(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody List<ExtensionMetricRequest> requests
    ) {
        Long userId = getUserId(principal);
        saveExtensionMetricsUseCase.execute(
                extensionPresentationMapper.toSaveMetricsRequest(userId, requests));
        return ResponseEntity.ok().build();
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null)
            throw new UnauthorizedException("Not authenticated");

        return Long.parseLong(principal.getUsername());
    }
}
