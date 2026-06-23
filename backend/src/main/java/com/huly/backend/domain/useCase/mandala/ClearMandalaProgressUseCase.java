package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class ClearMandalaProgressUseCase {

    private final MandalaProgressRepository mandalaProgressRepository;
    private final ListAvailableMandalasUseCase listAvailableMandalasUseCase;

    @Transactional
    public void execute(Long userId, String mandalaId) {
        validateAvailable(userId, mandalaId);
        mandalaProgressRepository.deleteByUserIdAndMandalaId(userId, mandalaId);
    }

    private void validateAvailable(Long userId, String mandalaId) {
        boolean available = listAvailableMandalasUseCase.execute(userId).stream()
                .anyMatch(availableMandala -> availableMandala.getMandala().getId().equals(mandalaId));
        if (!available) {
            throw new ResourceNotFoundException("Mandala no disponible");
        }
    }
}
