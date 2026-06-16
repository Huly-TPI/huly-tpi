package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.LanternThought;
import com.huly.backend.domain.model.enums.LanternStatus;
import com.huly.backend.domain.repository.LanternThoughtRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.LanternThoughtEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.ILanternThoughtJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LanternThoughtRepositoryImpl implements LanternThoughtRepository {

    private final ILanternThoughtJpaRepository jpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public LanternThought save(Long userId, String text) {
        AppUserEntity user = appUserRepository.getReferenceById(userId);
        LanternThoughtEntity entity = LanternThoughtEntity.builder()
                .user(user)
                .text(text)
                .status(LanternStatus.ACTIVE)
                .workedOn(false)
                .createdAt(Instant.now())
                .build();
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<LanternThought> findAllByUserId(Long userId) {
        return jpaRepository
                .findAllByUser_IdAndStatusOrderByCreatedAtDesc(userId, LanternStatus.ACTIVE)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<LanternThought> findByIdAndUserId(Long id, Long userId) {
        return jpaRepository.findByIdAndUser_Id(id, userId).map(this::toDomain);
    }

    @Override
    public LanternThought updateStatus(Long id, LanternStatus status) {
        LanternThoughtEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("LanternThought no encontrado: " + id));
        entity.setStatus(status);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public void markWorkedOn(Long id) {
        LanternThoughtEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("LanternThought no encontrado: " + id));
        entity.setWorkedOn(true);
        jpaRepository.save(entity);
    }

    private LanternThought toDomain(LanternThoughtEntity entity) {
        return LanternThought.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .text(entity.getText())
                .status(entity.getStatus())
                .workedOn(entity.isWorkedOn())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
