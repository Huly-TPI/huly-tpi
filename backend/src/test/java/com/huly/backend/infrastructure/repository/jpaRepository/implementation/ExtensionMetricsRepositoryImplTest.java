package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.ExtensionMetricEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IExtensionMetricJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtensionMetricsRepositoryImplTest {

    private static final Long USER_ID = 1L;
    private static final Instant CREATED_AT = Instant.now();

    @Mock
    private IExtensionMetricJpaRepository extensionMetricJpaRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private ExtensionMetricsRepositoryImpl repository;

    @Test
    @DisplayName("Mapea las métricas y las guarda")
    void saveAllShouldMapMetricsAndSave() {
        AppUserEntity user = referencedUser();
        givenReferencedUser(user);

        saveAll(List.of(
                metric("x.com", 100, 50, 1, 0, CREATED_AT),
                metric("tiktok.com", 200, 120, 2, 1, null)));

        thenSavedMetricsMatch(user);
    }

    @Test
    @DisplayName("Devuelve la lista de dominios mapeada al buscar por userId")
    void findByUserIdShouldReturnMappedDomainList() {
        givenMetricsByUser(metricEntity("facebook.com", 50, 20, 0, 0, CREATED_AT));

        List<ExtensionMetric> result = findByUserId();

        thenSingleMetricMapped(result, "facebook.com", 50, 20, 0, 0, CREATED_AT);
    }

    @Test
    @DisplayName("Devuelve la lista de dominios mapeada de métricas con consentimiento")
    void findAllConsentingMetricsShouldReturnMappedDomainList() {
        givenConsentingMetrics(metricEntity("youtube.com", 500, 150, 3, 2, CREATED_AT));

        List<ExtensionMetric> result = findAllConsentingMetrics();

        thenSingleMetricMapped(result, "youtube.com", 500, 150, 3, 2, CREATED_AT);
    }

    // --- arrange ---
    private void givenReferencedUser(AppUserEntity user) {
        when(appUserRepository.getReferenceById(USER_ID)).thenReturn(user);
    }

    private void givenMetricsByUser(ExtensionMetricEntity... entities) {
        when(extensionMetricJpaRepository.findByAppUserId(USER_ID)).thenReturn(List.of(entities));
    }

    private void givenConsentingMetrics(ExtensionMetricEntity... entities) {
        when(extensionMetricJpaRepository.findAllConsentingMetrics()).thenReturn(List.of(entities));
    }

    private AppUserEntity referencedUser() {
        return AppUserEntity.builder().id(USER_ID).build();
    }

    private ExtensionMetric metric(String domain, int activeSeconds, int scrollCount,
                                   int modalsShown, int redirects, Instant createdAt) {
        return ExtensionMetric.builder()
                .domain(domain)
                .activeSeconds(activeSeconds)
                .scrollCount(scrollCount)
                .modalsShown(modalsShown)
                .redirects(redirects)
                .createdAt(createdAt)
                .build();
    }

    private ExtensionMetricEntity metricEntity(String domain, int activeSeconds, int scrollCount,
                                               int modalsShown, int redirects, Instant createdAt) {
        return ExtensionMetricEntity.builder()
                .domain(domain)
                .activeSeconds(activeSeconds)
                .scrollCount(scrollCount)
                .modalsShown(modalsShown)
                .redirects(redirects)
                .createdAt(createdAt)
                .build();
    }

    // --- act ---
    private void saveAll(List<ExtensionMetric> metrics) {
        repository.saveAll(USER_ID, metrics);
    }

    private List<ExtensionMetric> findByUserId() {
        return repository.findByUserId(USER_ID);
    }

    private List<ExtensionMetric> findAllConsentingMetrics() {
        return repository.findAllConsentingMetrics();
    }

    // --- assert ---
    private void thenSavedMetricsMatch(AppUserEntity user) {
        ArgumentCaptor<List<ExtensionMetricEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(extensionMetricJpaRepository).saveAll(captor.capture());

        List<ExtensionMetricEntity> savedEntities = captor.getValue();
        assertThat(savedEntities).hasSize(2);

        ExtensionMetricEntity e1 = savedEntities.get(0);
        assertThat(e1.getAppUser()).isEqualTo(user);
        assertThat(e1.getDomain()).isEqualTo("x.com");
        assertThat(e1.getActiveSeconds()).isEqualTo(100);
        assertThat(e1.getScrollCount()).isEqualTo(50);
        assertThat(e1.getModalsShown()).isEqualTo(1);
        assertThat(e1.getRedirects()).isEqualTo(0);
        assertThat(e1.getCreatedAt()).isEqualTo(CREATED_AT);

        ExtensionMetricEntity e2 = savedEntities.get(1);
        assertThat(e2.getAppUser()).isEqualTo(user);
        assertThat(e2.getDomain()).isEqualTo("tiktok.com");
        assertThat(e2.getCreatedAt()).isNotNull();
    }

    private void thenSingleMetricMapped(List<ExtensionMetric> result, String domain, int activeSeconds,
                                        int scrollCount, int modalsShown, int redirects, Instant createdAt) {
        assertThat(result).hasSize(1);
        ExtensionMetric mapped = result.get(0);
        assertThat(mapped.getDomain()).isEqualTo(domain);
        assertThat(mapped.getActiveSeconds()).isEqualTo(activeSeconds);
        assertThat(mapped.getScrollCount()).isEqualTo(scrollCount);
        assertThat(mapped.getModalsShown()).isEqualTo(modalsShown);
        assertThat(mapped.getRedirects()).isEqualTo(redirects);
        assertThat(mapped.getCreatedAt()).isEqualTo(createdAt);
    }
}
