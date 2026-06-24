package com.huly.backend.domain.useCase.cloud;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.CloudThought;
import com.huly.backend.domain.model.enums.CloudStatus;
import com.huly.backend.domain.repository.CloudThoughtRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateCloudStatusUseCase {

    private final CloudThoughtRepository cloudThoughtRepository;

    public CloudThought execute(Long id, Long userId, CloudStatus newStatus) {
        CloudThought thought = cloudThoughtRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CloudThought", "id", id));

        if (thought.getStatus() != CloudStatus.ACTIVE) {
            throw new IllegalStateException("Solo se puede cambiar el estado de una nube activa");
        }
        if (newStatus != CloudStatus.COMPLETED && newStatus != CloudStatus.CANCELLED) {
            throw new IllegalArgumentException("Transición de estado no permitida: " + newStatus);
        }

        return cloudThoughtRepository.updateStatus(id, newStatus);
    }
}
