package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.MandalaProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IMandalaProgressJpaRepository extends JpaRepository<MandalaProgressEntity, MandalaProgressEntity.MandalaProgressId> {
    Optional<MandalaProgressEntity> findByUserIdAndMandalaId(Long userId, String mandalaId);
    void deleteByUserIdAndMandalaId(Long userId, String mandalaId);
}
