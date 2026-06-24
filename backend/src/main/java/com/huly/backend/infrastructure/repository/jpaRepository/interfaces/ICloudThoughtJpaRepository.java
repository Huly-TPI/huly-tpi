package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.domain.model.enums.CloudStatus;
import com.huly.backend.infrastructure.repository.entity.CloudThoughtEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ICloudThoughtJpaRepository extends JpaRepository<CloudThoughtEntity, Long> {
    List<CloudThoughtEntity> findAllByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, CloudStatus status);
    Optional<CloudThoughtEntity> findByIdAndUser_Id(Long id, Long userId);
}
