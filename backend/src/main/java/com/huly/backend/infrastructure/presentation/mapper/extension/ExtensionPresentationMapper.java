package com.huly.backend.infrastructure.presentation.mapper.extension;

import com.huly.backend.domain.dto.extension.ExtensionMetricItem;
import com.huly.backend.domain.dto.extension.GetUserAntiScrollSettingsRequest;
import com.huly.backend.domain.dto.extension.GetUserAntiScrollSettingsResponse;
import com.huly.backend.domain.dto.extension.SaveExtensionMetricsRequest;
import com.huly.backend.domain.dto.extension.SaveUserAntiScrollSettingsRequest;
import com.huly.backend.infrastructure.presentation.dto.extension.AntiScrollSettingsRequest;
import com.huly.backend.infrastructure.presentation.dto.extension.AntiScrollSettingsResponse;
import com.huly.backend.infrastructure.presentation.dto.extension.ExtensionMetricRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper de presentacion para el feature de extension:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class ExtensionPresentationMapper {

    public GetUserAntiScrollSettingsRequest toGetSettingsRequest(Long userId) {
        return new GetUserAntiScrollSettingsRequest(userId);
    }

    public SaveUserAntiScrollSettingsRequest toSaveSettingsRequest(Long userId, AntiScrollSettingsRequest request) {
        return new SaveUserAntiScrollSettingsRequest(
                userId,
                request.isEnabled(),
                request.getPauseIntervalSeconds(),
                request.getMonitoredDomains(),
                request.isDataSharingConsent()
        );
    }

    public SaveExtensionMetricsRequest toSaveMetricsRequest(Long userId, List<ExtensionMetricRequest> requests) {
        List<ExtensionMetricItem> metrics = requests.stream()
                .map(this::toMetricItem)
                .toList();
        return new SaveExtensionMetricsRequest(userId, metrics);
    }

    public AntiScrollSettingsResponse toSettingsResponse(GetUserAntiScrollSettingsResponse settings) {
        return AntiScrollSettingsResponse.builder()
                .enabled(settings.enabled())
                .pauseIntervalSeconds(settings.pauseIntervalSeconds())
                .gardenUrl(settings.gardenUrl())
                .backendUrl(settings.backendUrl())
                .monitoredDomains(settings.monitoredDomains())
                .dataSharingConsent(settings.dataSharingConsent())
                .userName(settings.userName())
                .termsAndConditions(settings.termsAndConditions())
                .build();
    }

    private ExtensionMetricItem toMetricItem(ExtensionMetricRequest request) {
        return new ExtensionMetricItem(
                request.getDomain(),
                request.getActiveSeconds(),
                request.getScrollCount(),
                request.getModalsShown(),
                request.getRedirects()
        );
    }
}
