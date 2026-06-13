package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.ExtensionMetricEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IExtensionMetricJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExtensionMetricsRepositoryImpl implements ExtensionMetricsRepository {

    private final IExtensionMetricJpaRepository extensionMetricJpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public void saveAll(Long userId, List<ExtensionMetric> metrics) {
        AppUserEntity user = appUserRepository.getReferenceById(userId);
        
        List<ExtensionMetricEntity> entities = metrics.stream()
                .map(m -> ExtensionMetricEntity.builder()
                        .appUser(user)
                        .domain(m.getDomain())
                        .activeSeconds(m.getActiveSeconds())
                        .scrollCount(m.getScrollCount())
                        .modalsShown(m.getModalsShown())
                        .redirects(m.getRedirects())
                        .build())
                .toList();

        extensionMetricJpaRepository.saveAll(entities);
    }

    @Override
    public List<ExtensionMetric> findByUserId(Long userId) {
        return extensionMetricJpaRepository.findByAppUserId(userId).stream()
                .map(entity -> ExtensionMetric.builder()
                        .domain(entity.getDomain())
                        .activeSeconds(entity.getActiveSeconds())
                        .scrollCount(entity.getScrollCount())
                        .modalsShown(entity.getModalsShown())
                        .redirects(entity.getRedirects())
                        .build())
                .toList();
    }

    @Override
    public List<ExtensionMetric> findAllConsentingMetrics() {
        return extensionMetricJpaRepository.findAllConsentingMetrics().stream()
                .map(entity -> ExtensionMetric.builder()
                        .domain(entity.getDomain())
                        .activeSeconds(entity.getActiveSeconds())
                        .scrollCount(entity.getScrollCount())
                        .modalsShown(entity.getModalsShown())
                        .redirects(entity.getRedirects())
                        .build())
                .toList();
    }
}
