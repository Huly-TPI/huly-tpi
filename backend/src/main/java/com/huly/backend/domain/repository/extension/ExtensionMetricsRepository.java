package com.huly.backend.domain.repository.extension;

import com.huly.backend.domain.model.extension.ExtensionMetric;
import java.util.List;

public interface ExtensionMetricsRepository {
    void saveAll(Long userId, List<ExtensionMetric> metrics);
    List<ExtensionMetric> findByUserId(Long userId);
    List<ExtensionMetric> findAllConsentingMetrics();
}
