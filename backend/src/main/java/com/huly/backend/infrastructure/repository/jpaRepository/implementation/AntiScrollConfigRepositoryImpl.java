package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.extension.AntiScrollConfig;
import com.huly.backend.domain.repository.extension.AntiScrollConfigRepository;
import com.huly.backend.infrastructure.repository.entity.AntiScrollConfigEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IAntiScrollConfigJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.AntiScrollConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AntiScrollConfigRepositoryImpl implements AntiScrollConfigRepository {

    private final IAntiScrollConfigJpaRepository jpa;
    private final AntiScrollConfigMapper mapper;

    @Override
    public Optional<AntiScrollConfig> findFirst() {
        return jpa.findAll().stream().findFirst().map(mapper::toDomain);
    }

    @Override
    public AntiScrollConfig save(AntiScrollConfig config) {
        AntiScrollConfigEntity entity = mapper.toEntity(config);
        AntiScrollConfigEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }
}
