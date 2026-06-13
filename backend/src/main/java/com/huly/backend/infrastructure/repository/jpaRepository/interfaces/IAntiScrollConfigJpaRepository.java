package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.AntiScrollConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAntiScrollConfigJpaRepository extends JpaRepository<AntiScrollConfigEntity, Long> {
}
