package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;
import com.huly.backend.infrastructure.repository.entity.BreathingTechniquesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBreathingTechniqueJpaRepository extends JpaRepository<BreathingTechniquesEntity, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}