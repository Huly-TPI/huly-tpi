package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.ExtensionMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IExtensionMetricJpaRepository extends JpaRepository<ExtensionMetricEntity, Long> {
}
