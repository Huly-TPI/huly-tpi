package com.huly.backend.domain.useCase.lantern;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.LanternThought;
import com.huly.backend.domain.model.enums.LanternStatus;
import com.huly.backend.domain.repository.LanternThoughtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateLanternStatusUseCase {

    private final LanternThoughtRepository lanternThoughtRepository;

    public LanternThought execute(Long id, Long userId, LanternStatus newStatus) {
        LanternThought thought = lanternThoughtRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("LanternThought", "id", id));

        if (thought.getStatus() != LanternStatus.ACTIVE) {
            throw new IllegalStateException("Solo se puede cambiar el estado de un farolito activo");
        }
        if (newStatus != LanternStatus.COMPLETED && newStatus != LanternStatus.CANCELLED) {
            throw new IllegalArgumentException("Transición de estado no permitida: " + newStatus);
        }

        return lanternThoughtRepository.updateStatus(id, newStatus);
    }
}
