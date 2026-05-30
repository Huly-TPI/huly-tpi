package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.UserEmotionalState;
import com.huly.backend.domain.repository.UserEmotionalStateRepository;
import com.huly.backend.infrastructure.repository.entity.UserEmotionalStateEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserEmotionalStateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserEmotionalStateRepositoryImpl implements UserEmotionalStateRepository {

    private final IUserEmotionalStateJpaRepository jpaRepository;

    @Override
    public UserEmotionalState save(UserEmotionalState state) {
        UserEmotionalStateEntity entity = toEntity(state);
        UserEmotionalStateEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    } 

    private UserEmotionalStateEntity toEntity(UserEmotionalState state) {
        return UserEmotionalStateEntity.builder()
                .userId(state.getUserId())
                .valence(state.getValence())
                .arousal(state.getArousal())
                .dominance(state.getDominance())
                .intensity(state.getIntensity())
                .source(state.getSource())
                .timestamp(state.getTimestamp())
                .build();
    }

    private UserEmotionalState toDomain(UserEmotionalStateEntity entity) {
        return UserEmotionalState.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .valence(entity.getValence())
                .arousal(entity.getArousal())
                .dominance(entity.getDominance())
                .intensity(entity.getIntensity())
                .source(entity.getSource())
                .timestamp(entity.getTimestamp())
                .build();
    }


    
}
