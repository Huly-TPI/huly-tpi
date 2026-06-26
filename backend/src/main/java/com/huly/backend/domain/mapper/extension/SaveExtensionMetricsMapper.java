package com.huly.backend.domain.mapper.extension;

import com.huly.backend.domain.dto.extension.ExtensionMetricItem;
import com.huly.backend.domain.dto.extension.SaveExtensionMetricsRequest;
import com.huly.backend.domain.dto.extension.SaveExtensionMetricsResponse;
import com.huly.backend.domain.model.extension.ExtensionMetric;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de guardado de metricas de la extension.
 */
public class SaveExtensionMetricsMapper {

    public List<ExtensionMetric> toModel(SaveExtensionMetricsRequest request) {
        return request.metrics().stream()
                .map(this::toModel)
                .toList();
    }

    private ExtensionMetric toModel(ExtensionMetricItem item) {
        return ExtensionMetric.builder()
                .domain(item.domain())
                .activeSeconds(item.activeSeconds())
                .scrollCount(item.scrollCount())
                .modalsShown(item.modalsShown())
                .redirects(item.redirects())
                .build();
    }

    public SaveExtensionMetricsResponse toResponse() {
        return new SaveExtensionMetricsResponse();
    }
}
