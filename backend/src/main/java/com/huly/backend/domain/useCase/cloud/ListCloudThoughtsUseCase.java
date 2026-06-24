package com.huly.backend.domain.useCase.cloud;

import com.huly.backend.domain.model.CloudThought;
import com.huly.backend.domain.repository.CloudThoughtRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListCloudThoughtsUseCase {

    private final CloudThoughtRepository cloudThoughtRepository;

    public List<CloudThought> execute(Long userId) {
        return cloudThoughtRepository.findAllByUserId(userId);
    }
}
