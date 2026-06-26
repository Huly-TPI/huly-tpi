package com.huly.backend.infrastructure.presentation.mapper.emotionalEvent;

import com.huly.backend.domain.dto.emotionalEvent.SaveUserEmotionalStateRequest;
import com.huly.backend.domain.dto.emotionalEvent.SaveUserEmotionalStateResponse;
import com.huly.backend.infrastructure.presentation.dto.emotionalState.UserEmotionalStateRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalState.UserEmotionalStateResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de estados emocionales del usuario:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class UserEmotionalStatePresentationMapper {

    public SaveUserEmotionalStateRequest toSaveRequest(UserEmotionalStateRequest request) {
        return new SaveUserEmotionalStateRequest(
                request.userId(),
                request.valence(),
                request.arousal(),
                request.dominance(),
                request.intensity(),
                request.source()
        );
    }

    public UserEmotionalStateResponse toStateResponse(SaveUserEmotionalStateResponse response) {
        return new UserEmotionalStateResponse(
                response.id(),
                response.userId(),
                response.valence(),
                response.arousal(),
                response.dominance(),
                response.intensity(),
                response.source(),
                response.timestamp()
        );
    }
}
