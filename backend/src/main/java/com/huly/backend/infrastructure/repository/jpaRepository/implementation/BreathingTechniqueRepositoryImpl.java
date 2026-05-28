package com.huly.backend.infrastructure.repository.jpaRepository.implementation;
import com.huly.backend.domain.model.BreathingTechnique;
import com.huly.backend.domain.repository.BreathingTechniqueRepository;
import com.huly.backend.infrastructure.repository.entity.BreathingTechniquesEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IBreathingTechniqueJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List; 

@Component
@RequiredArgsConstructor
public class BreathingTechniqueRepositoryImpl implements BreathingTechniqueRepository {
    private final IBreathingTechniqueJpaRepository jpaRepository;

    @Override
    public List<BreathingTechnique> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private BreathingTechnique toDomain(BreathingTechniquesEntity entity) {
        return BreathingTechnique.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .inhaleSeconds(entity.getInhaleSeconds())
                .holdSeconds(entity.getHoldSeconds())
                .exhaleSeconds(entity.getExhaleSeconds())
                .roundsInterval(entity.getRoundsInterval())
                .rounds(entity.getRounds())
                .build();
    }
     }