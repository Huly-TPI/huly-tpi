package com.huly.backend.domain.useCase.cloud;

import com.huly.backend.domain.dto.cloud.ListCloudThoughtsRequest;
import com.huly.backend.domain.dto.cloud.ListCloudThoughtsResponse;
import com.huly.backend.domain.mapper.cloud.ListCloudThoughtsMapper;
import com.huly.backend.domain.repository.CloudThoughtRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListCloudThoughtsUseCase {

    private final CloudThoughtRepository cloudThoughtRepository;
    private final ListCloudThoughtsMapper mapper;

    public ListCloudThoughtsResponse execute(ListCloudThoughtsRequest request) {
        return mapper.toResponse(cloudThoughtRepository.findAllByUserId(request.userId()));
    }
}
