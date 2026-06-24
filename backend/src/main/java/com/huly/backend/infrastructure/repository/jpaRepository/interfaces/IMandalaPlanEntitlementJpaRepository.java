package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.MandalaPlanEntitlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IMandalaPlanEntitlementJpaRepository extends JpaRepository<MandalaPlanEntitlementEntity, Long> {
    List<MandalaPlanEntitlementEntity> findAllByPlanCode(String planCode);
}
