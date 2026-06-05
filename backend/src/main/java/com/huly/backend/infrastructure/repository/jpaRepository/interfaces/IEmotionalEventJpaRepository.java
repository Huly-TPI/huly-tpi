package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.EmotionalEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IEmotionalEventJpaRepository extends JpaRepository<EmotionalEventEntity, Long> {
}
