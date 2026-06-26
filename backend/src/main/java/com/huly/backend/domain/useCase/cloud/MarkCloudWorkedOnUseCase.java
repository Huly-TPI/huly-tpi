package com.huly.backend.domain.useCase.cloud;

import com.huly.backend.domain.dto.cloud.MarkCloudWorkedOnRequest;
import com.huly.backend.domain.dto.cloud.MarkCloudWorkedOnResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.cloud.MarkCloudWorkedOnMapper;
import com.huly.backend.domain.repository.CloudThoughtRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MarkCloudWorkedOnUseCase {

    private final CloudThoughtRepository cloudThoughtRepository;
    private final MarkCloudWorkedOnMapper mapper;

    public MarkCloudWorkedOnResponse execute(MarkCloudWorkedOnRequest request) {
        cloudThoughtRepository.findByIdAndUserId(request.id(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("CloudThought", "id", request.id()));
        cloudThoughtRepository.markWorkedOn(request.id());
        return mapper.toResponse(request.id());
    }
}
