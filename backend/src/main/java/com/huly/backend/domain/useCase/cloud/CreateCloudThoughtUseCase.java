package com.huly.backend.domain.useCase.cloud;

import com.huly.backend.domain.dto.cloud.CreateCloudThoughtRequest;
import com.huly.backend.domain.dto.cloud.CreateCloudThoughtResponse;
import com.huly.backend.domain.mapper.cloud.CreateCloudThoughtMapper;
import com.huly.backend.domain.model.CloudThought;
import com.huly.backend.domain.repository.CloudThoughtRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateCloudThoughtUseCase {

    private final CloudThoughtRepository cloudThoughtRepository;
    private final CreateCloudThoughtMapper mapper;

    public CreateCloudThoughtResponse execute(CreateCloudThoughtRequest request) {
        CloudThought saved = cloudThoughtRepository.save(request.userId(), request.text());
        return mapper.toResponse(saved);
    }
}
