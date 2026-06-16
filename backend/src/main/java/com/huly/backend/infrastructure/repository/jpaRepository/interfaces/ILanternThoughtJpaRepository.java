package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.domain.model.enums.LanternStatus;
import com.huly.backend.infrastructure.repository.entity.LanternThoughtEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ILanternThoughtJpaRepository extends JpaRepository<LanternThoughtEntity, Long> {
    List<LanternThoughtEntity> findAllByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, LanternStatus status);
    Optional<LanternThoughtEntity> findByIdAndUser_Id(Long id, Long userId);
}
