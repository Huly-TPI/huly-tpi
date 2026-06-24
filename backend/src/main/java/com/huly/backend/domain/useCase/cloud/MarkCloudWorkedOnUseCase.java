package com.huly.backend.domain.useCase.cloud;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.repository.CloudThoughtRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MarkCloudWorkedOnUseCase {

    private final CloudThoughtRepository cloudThoughtRepository;

    public void execute(Long id, Long userId) {
        cloudThoughtRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CloudThought", "id", id));
        cloudThoughtRepository.markWorkedOn(id);
    }
}
