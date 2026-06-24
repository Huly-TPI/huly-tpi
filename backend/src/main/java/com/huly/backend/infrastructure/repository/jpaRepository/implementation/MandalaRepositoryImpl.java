package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.domain.model.mandala.Mandala;
import com.huly.backend.domain.repository.mandala.MandalaRepository;
import com.huly.backend.infrastructure.repository.entity.MandalaEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IMandalaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MandalaRepositoryImpl implements MandalaRepository {

    private final IMandalaJpaRepository jpaRepository;

    @Override
    public List<Mandala> findAllActiveOrderByDisplayOrder() {
        return jpaRepository.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Mandala> findAllActiveByAccessTypeOrderByDisplayOrder(MandalaAccessType accessType) {
        return jpaRepository.findAllByActiveTrueAndAccessTypeOrderByDisplayOrderAsc(accessType).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Mandala> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private Mandala toDomain(MandalaEntity entity) {
        return Mandala.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .assetKey(entity.getAssetKey())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.isActive())
                .accessType(entity.getAccessType())
                .priceCoins(entity.getPriceCoins())
                .build();
    }
}
