package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;
import com.huly.backend.infrastructure.repository.entity.BreathingTechniquesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IBreathingTechniqueJpaRepository extends JpaRepository<BreathingTechniquesEntity, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    List<BreathingTechniquesEntity> findByActive(boolean active);
    List<BreathingTechniquesEntity> findAllByOrderByIdAsc();
}