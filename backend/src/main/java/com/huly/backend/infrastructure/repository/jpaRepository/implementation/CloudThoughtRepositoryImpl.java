package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.CloudThought;
import com.huly.backend.domain.model.enums.CloudStatus;
import com.huly.backend.domain.repository.CloudThoughtRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.CloudThoughtEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.ICloudThoughtJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CloudThoughtRepositoryImpl implements CloudThoughtRepository {

    private final ICloudThoughtJpaRepository jpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public CloudThought save(Long userId, String text) {
        AppUserEntity user = appUserRepository.getReferenceById(userId);
        CloudThoughtEntity entity = CloudThoughtEntity.builder()
                .user(user)
                .text(text)
                .status(CloudStatus.ACTIVE)
                .workedOn(false)
                .createdAt(Instant.now())
                .build();
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<CloudThought> findAllByUserId(Long userId) {
        return jpaRepository
                .findAllByUser_IdAndStatusOrderByCreatedAtDesc(userId, CloudStatus.ACTIVE)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<CloudThought> findByIdAndUserId(Long id, Long userId) {
        return jpaRepository.findByIdAndUser_Id(id, userId).map(this::toDomain);
    }

    @Override
    public CloudThought updateStatus(Long id, CloudStatus status) {
        CloudThoughtEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CloudThought no encontrado: " + id));
        entity.setStatus(status);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public void markWorkedOn(Long id) {
        CloudThoughtEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CloudThought no encontrado: " + id));
        entity.setWorkedOn(true);
        jpaRepository.save(entity);
    }

    private CloudThought toDomain(CloudThoughtEntity entity) {
        return CloudThought.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .text(entity.getText())
                .status(entity.getStatus())
                .workedOn(entity.isWorkedOn())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
