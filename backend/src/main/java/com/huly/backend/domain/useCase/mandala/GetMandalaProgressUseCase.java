package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.mandala.MandalaProgress;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class GetMandalaProgressUseCase {

    private final MandalaProgressRepository mandalaProgressRepository;
    private final ListAvailableMandalasUseCase listAvailableMandalasUseCase;

    public Optional<byte[]> execute(Long userId, String mandalaId) {
        validateAvailable(userId, mandalaId);
        return mandalaProgressRepository.findByUserIdAndMandalaId(userId, mandalaId)
                .map(MandalaProgress::getPaintBlob);
    }

    private void validateAvailable(Long userId, String mandalaId) {
        boolean available = listAvailableMandalasUseCase.execute(userId).stream()
                .anyMatch(availableMandala -> availableMandala.getMandala().getId().equals(mandalaId));
        if (!available) {
            throw new ResourceNotFoundException("Mandala no disponible");
        }
    }
}
