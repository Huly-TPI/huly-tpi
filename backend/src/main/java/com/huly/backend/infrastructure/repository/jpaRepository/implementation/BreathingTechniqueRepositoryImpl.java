package com.huly.backend.infrastructure.repository.jpaRepository.implementation;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import com.huly.backend.infrastructure.repository.entity.BreathingTechniquesEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IBreathingTechniqueJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BreathingTechniqueRepositoryImpl implements BreathingTechniqueRepository {
    private final IBreathingTechniqueJpaRepository jpaRepository;

    @Override
    public List<BreathingTechnique> findAll() {
        return jpaRepository.findAllByOrderByIdAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<BreathingTechnique> findByActive(boolean active) {
        return jpaRepository.findByActive(active).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<BreathingTechnique> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public BreathingTechnique save(BreathingTechnique t) {
        BreathingTechniquesEntity entity = t.getId() != null
                ? jpaRepository.findById(t.getId()).orElseGet(BreathingTechniquesEntity::new)
                : new BreathingTechniquesEntity();
        entity.setName(t.getName());
        entity.setDescription(t.getDescription());
        entity.setInhaleSeconds(t.getInhaleSeconds());
        entity.setHoldSeconds(t.getHoldSeconds());
        entity.setExhaleSeconds(t.getExhaleSeconds());
        entity.setRoundsInterval(t.getRoundsInterval());
        entity.setRounds(t.getRounds());
        entity.setActive(t.isActive());
        return toDomain(jpaRepository.save(entity));
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
                .active(entity.isActive())
                .build();
    }
}