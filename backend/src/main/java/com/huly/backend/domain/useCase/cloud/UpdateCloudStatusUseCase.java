package com.huly.backend.domain.useCase.cloud;

import com.huly.backend.domain.dto.cloud.UpdateCloudStatusRequest;
import com.huly.backend.domain.dto.cloud.UpdateCloudStatusResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.cloud.UpdateCloudStatusMapper;
import com.huly.backend.domain.model.CloudThought;
import com.huly.backend.domain.model.enums.CloudStatus;
import com.huly.backend.domain.repository.CloudThoughtRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateCloudStatusUseCase {

    private final CloudThoughtRepository cloudThoughtRepository;
    private final UpdateCloudStatusMapper mapper;

    public UpdateCloudStatusResponse execute(UpdateCloudStatusRequest request) {
        CloudThought thought = cloudThoughtRepository.findByIdAndUserId(request.id(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("CloudThought", "id", request.id()));

        if (thought.getStatus() != CloudStatus.ACTIVE) {
            throw new IllegalStateException("Solo se puede cambiar el estado de una nube activa");
        }
        CloudStatus newStatus = request.newStatus();
        if (newStatus != CloudStatus.COMPLETED && newStatus != CloudStatus.CANCELLED) {
            throw new IllegalArgumentException("Transición de estado no permitida: " + newStatus);
        }

        CloudThought updated = cloudThoughtRepository.updateStatus(request.id(), newStatus);
        return mapper.toResponse(updated);
    }
}
