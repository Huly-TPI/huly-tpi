package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.ExtensionMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface IExtensionMetricJpaRepository extends JpaRepository<ExtensionMetricEntity, Long> {
    List<ExtensionMetricEntity> findByAppUserId(Long userId);

    @Query("SELECT m FROM ExtensionMetricEntity m WHERE EXISTS (SELECT s FROM UserSettingEntity s WHERE s.appUser = m.appUser AND s.dataSharingConsent = true)")
    List<ExtensionMetricEntity> findAllConsentingMetrics();
}
