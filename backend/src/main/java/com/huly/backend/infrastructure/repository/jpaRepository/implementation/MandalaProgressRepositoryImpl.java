package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.mandala.MandalaProgress;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import com.huly.backend.infrastructure.repository.entity.MandalaProgressEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IMandalaProgressJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MandalaProgressRepositoryImpl implements MandalaProgressRepository {

    private final IMandalaProgressJpaRepository jpaRepository;

    @Override
    public MandalaProgress save(MandalaProgress mandalaProgress) {
        MandalaProgressEntity entity = jpaRepository
                .findByUserIdAndMandalaId(mandalaProgress.getUserId(), mandalaProgress.getMandalaId())
                .orElseGet(() -> MandalaProgressEntity.builder()
                        .userId(mandalaProgress.getUserId())
                        .mandalaId(mandalaProgress.getMandalaId())
                        .sessionRegistered(mandalaProgress.isSessionRegistered())
                        .build());
        entity.setPaintBlob(mandalaProgress.getPaintBlob());
        
        MandalaProgressEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MandalaProgress> findByUserIdAndMandalaId(Long userId, String mandalaId) {
        return jpaRepository.findByUserIdAndMandalaId(userId, mandalaId).map(this::toDomain);
    }

    @Override
    public void markSessionRegistered(Long userId, String mandalaId) {
        jpaRepository.findByUserIdAndMandalaId(userId, mandalaId).ifPresent(entity -> {
            entity.setSessionRegistered(true);
            jpaRepository.save(entity);
        });
    }

    @Override
    public void deleteByUserIdAndMandalaId(Long userId, String mandalaId) {
        jpaRepository.deleteByUserIdAndMandalaId(userId, mandalaId);
    }

    private MandalaProgress toDomain(MandalaProgressEntity entity) {
        return MandalaProgress.builder()
                .userId(entity.getUserId())
                .mandalaId(entity.getMandalaId())
                .paintBlob(entity.getPaintBlob())
                .sessionRegistered(entity.isSessionRegistered())
                .build();
    }
}
