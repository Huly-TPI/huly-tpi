package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.dto.emotionalEvent.SaveUserEmotionalStateRequest;
import com.huly.backend.domain.dto.emotionalEvent.SaveUserEmotionalStateResponse;
import com.huly.backend.domain.mapper.emotionalEvent.SaveUserEmotionalStateMapper;
import com.huly.backend.domain.model.user.UserEmotionalState;
import com.huly.backend.domain.repository.user.UserEmotionalStateRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveUserEmotionalStateUseCase {
    private final UserEmotionalStateRepository userEmotionalStateRepository;
    private final SaveUserEmotionalStateMapper mapper;

    public SaveUserEmotionalStateResponse execute(SaveUserEmotionalStateRequest request) {
        UserEmotionalState saved = userEmotionalStateRepository.save(mapper.toModel(request));
        return mapper.toResponse(saved);
    }
}
