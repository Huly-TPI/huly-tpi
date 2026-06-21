package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.repository.badge.BadgeRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IBadgeJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.BadgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BadgeRepositoryImpl implements BadgeRepository {
    private final IBadgeJpaRepository badgeJpaRepository;
    private final BadgeMapper badgeMapper;

    @Override
    public List<Badge> findAll() {
        return badgeJpaRepository.findAll().stream().map(badgeMapper::toDomain).toList();
    }

    @Override
    public Optional<Badge> findByCode(String code) {
        return badgeJpaRepository.findByCode(code).map(badgeMapper::toDomain);
    }
}