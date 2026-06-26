package com.huly.backend.domain.mapper.emotionalEvent;

import com.huly.backend.domain.dto.emotionalEvent.SaveUserEmotionalStateRequest;
import com.huly.backend.domain.dto.emotionalEvent.SaveUserEmotionalStateResponse;
import com.huly.backend.domain.model.user.UserEmotionalState;

import java.time.Instant;

/**
 * Mapper de dominio para el caso de uso de guardado del estado emocional del usuario.
 */
public class SaveUserEmotionalStateMapper {

    public UserEmotionalState toModel(SaveUserEmotionalStateRequest request) {
        return UserEmotionalState.builder()
                .userId(request.userId())
                .valence(request.valence())
                .arousal(request.arousal())
                .dominance(request.dominance())
                .intensity(request.intensity())
                .source(request.source())
                .timestamp(Instant.now())
                .build();
    }

    public SaveUserEmotionalStateResponse toResponse(UserEmotionalState state) {
        return new SaveUserEmotionalStateResponse(
                state.getId(),
                state.getUserId(),
                state.getValence(),
                state.getArousal(),
                state.getDominance(),
                state.getIntensity(),
                state.getSource(),
                state.getTimestamp()
        );
    }
}
