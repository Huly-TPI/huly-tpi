package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.domain.repository.extension.AntiScrollGlobalConfigRepository;
import com.huly.backend.infrastructure.repository.entity.AntiScrollConfigEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IAntiScrollConfigJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.AntiScrollGlobalConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AntiScrollGlobalConfigRepositoryImpl implements AntiScrollGlobalConfigRepository {

    private final IAntiScrollConfigJpaRepository jpa;
    private final AntiScrollGlobalConfigMapper mapper;

    @Override
    public Optional<AntiScrollGlobalConfig> findFirst() {
        return jpa.findAll().stream().findFirst().map(mapper::toDomain);
    }

    @Override
    public AntiScrollGlobalConfig save(AntiScrollGlobalConfig config) {
        AntiScrollConfigEntity entity = mapper.toEntity(config);
        AntiScrollConfigEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }
}
