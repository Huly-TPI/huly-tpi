package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.mandala.MandalaProgress;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveMandalaProgressUseCase {

    private final MandalaProgressRepository mandalaProgressRepository;
    private final ListAvailableMandalasUseCase listAvailableMandalasUseCase;

    public void execute(Long userId, String mandalaId, byte[] paintBlob) {
        validateAvailable(userId, mandalaId);
        MandalaProgress progress = MandalaProgress.builder()
                .userId(userId)
                .mandalaId(mandalaId)
                .paintBlob(paintBlob)
                .build();
        mandalaProgressRepository.save(progress);
    }

    private void validateAvailable(Long userId, String mandalaId) {
        boolean available = listAvailableMandalasUseCase.execute(userId).stream()
                .anyMatch(availableMandala -> availableMandala.getMandala().getId().equals(mandalaId));
        if (!available) {
            throw new ResourceNotFoundException("Mandala no disponible");
        }
    }
}
