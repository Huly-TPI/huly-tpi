package com.huly.backend.domain.useCase.cloud;

import com.huly.backend.domain.model.CloudThought;
import com.huly.backend.domain.repository.CloudThoughtRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateCloudThoughtUseCase {

    private final CloudThoughtRepository cloudThoughtRepository;

    public CloudThought execute(Long userId, String text) {
        return cloudThoughtRepository.save(userId, text);
    }
}
