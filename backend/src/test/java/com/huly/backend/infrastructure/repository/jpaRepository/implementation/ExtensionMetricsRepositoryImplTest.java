package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.ExtensionMetricEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IExtensionMetricJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtensionMetricsRepositoryImplTest {

    @Mock
    private IExtensionMetricJpaRepository extensionMetricJpaRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private ExtensionMetricsRepositoryImpl repository;

    @Test
    void saveAll_shouldMapMetricsAndSave() {
        Long userId = 1L;
        AppUserEntity mockUser = AppUserEntity.builder().id(userId).build();
        when(appUserRepository.getReferenceById(userId)).thenReturn(mockUser);

        Instant time = Instant.now();
        ExtensionMetric metric1 = ExtensionMetric.builder()
                .domain("x.com")
                .activeSeconds(100)
                .scrollCount(50)
                .modalsShown(1)
                .redirects(0)
                .createdAt(time)
                .build();

        ExtensionMetric metric2 = ExtensionMetric.builder()
                .domain("tiktok.com")
                .activeSeconds(200)
                .scrollCount(120)
                .modalsShown(2)
                .redirects(1)
                .createdAt(null) // Should fall back to Instant.now()
                .build();

        repository.saveAll(userId, List.of(metric1, metric2));

        ArgumentCaptor<List<ExtensionMetricEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(extensionMetricJpaRepository).saveAll(captor.capture());

        List<ExtensionMetricEntity> savedEntities = captor.getValue();
        assertThat(savedEntities).hasSize(2);

        ExtensionMetricEntity e1 = savedEntities.get(0);
        assertThat(e1.getAppUser()).isEqualTo(mockUser);
        assertThat(e1.getDomain()).isEqualTo("x.com");
        assertThat(e1.getActiveSeconds()).isEqualTo(100);
        assertThat(e1.getScrollCount()).isEqualTo(50);
        assertThat(e1.getModalsShown()).isEqualTo(1);
        assertThat(e1.getRedirects()).isEqualTo(0);
        assertThat(e1.getCreatedAt()).isEqualTo(time);

        ExtensionMetricEntity e2 = savedEntities.get(1);
        assertThat(e2.getAppUser()).isEqualTo(mockUser);
        assertThat(e2.getDomain()).isEqualTo("tiktok.com");
        assertThat(e2.getCreatedAt()).isNotNull();
    }

    @Test
    void findByUserId_shouldReturnMappedDomainList() {
        Long userId = 1L;
        Instant time = Instant.now();
        ExtensionMetricEntity entity = ExtensionMetricEntity.builder()
                .domain("facebook.com")
                .activeSeconds(50)
                .scrollCount(20)
                .modalsShown(0)
                .redirects(0)
                .createdAt(time)
                .build();

        when(extensionMetricJpaRepository.findByAppUserId(userId)).thenReturn(List.of(entity));

        List<ExtensionMetric> result = repository.findByUserId(userId);

        assertThat(result).hasSize(1);
        ExtensionMetric mapped = result.get(0);
        assertThat(mapped.getDomain()).isEqualTo("facebook.com");
        assertThat(mapped.getActiveSeconds()).isEqualTo(50);
        assertThat(mapped.getScrollCount()).isEqualTo(20);
        assertThat(mapped.getModalsShown()).isEqualTo(0);
        assertThat(mapped.getRedirects()).isEqualTo(0);
        assertThat(mapped.getCreatedAt()).isEqualTo(time);
    }

    @Test
    void findAllConsentingMetrics_shouldReturnMappedDomainList() {
        Instant time = Instant.now();
        ExtensionMetricEntity entity = ExtensionMetricEntity.builder()
                .domain("youtube.com")
                .activeSeconds(500)
                .scrollCount(150)
                .modalsShown(3)
                .redirects(2)
                .createdAt(time)
                .build();

        when(extensionMetricJpaRepository.findAllConsentingMetrics()).thenReturn(List.of(entity));

        List<ExtensionMetric> result = repository.findAllConsentingMetrics();

        assertThat(result).hasSize(1);
        ExtensionMetric mapped = result.get(0);
        assertThat(mapped.getDomain()).isEqualTo("youtube.com");
        assertThat(mapped.getActiveSeconds()).isEqualTo(500);
        assertThat(mapped.getScrollCount()).isEqualTo(150);
        assertThat(mapped.getModalsShown()).isEqualTo(3);
        assertThat(mapped.getRedirects()).isEqualTo(2);
        assertThat(mapped.getCreatedAt()).isEqualTo(time);
    }
}
