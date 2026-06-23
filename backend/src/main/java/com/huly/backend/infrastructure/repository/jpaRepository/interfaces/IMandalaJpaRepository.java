package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.infrastructure.repository.entity.MandalaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IMandalaJpaRepository extends JpaRepository<MandalaEntity, String> {
    List<MandalaEntity> findAllByActiveTrueOrderByDisplayOrderAsc();
    List<MandalaEntity> findAllByActiveTrueAndAccessTypeOrderByDisplayOrderAsc(MandalaAccessType accessType);
}
