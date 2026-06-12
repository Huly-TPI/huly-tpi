package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.model.UserEmotionalState;
import com.huly.backend.domain.repository.UserEmotionalStateRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class SaveUserEmotionalStateUseCase {
    private final UserEmotionalStateRepository userEmotionalStateRepository;
    public UserEmotionalState execute(Long userId, double valence, double arousal, double dominance, double intensity, String source) {
        UserEmotionalState state = UserEmotionalState.builder()
                .userId(userId)
                .valence(valence)
                .arousal(arousal)
                .dominance(dominance)
                .intensity(intensity)
                .source(source)
                .timestamp(Instant.now())
                .build();
        return userEmotionalStateRepository.save(state);
    }
}
