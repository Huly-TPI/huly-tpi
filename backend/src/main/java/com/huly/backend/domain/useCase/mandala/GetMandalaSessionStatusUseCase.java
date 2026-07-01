package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.dto.mandala.GetMandalaSessionStatusRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaSessionStatusResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.mandala.GetMandalaSessionStatusMapper;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetMandalaSessionStatusUseCase {

    private final MandalaProgressRepository mandalaProgressRepository;
    private final ListAvailableMandalasUseCase listAvailableMandalasUseCase;
    private final GetMandalaSessionStatusMapper mapper;

    public GetMandalaSessionStatusResponse execute(GetMandalaSessionStatusRequest request) {
        validateAvailableMandala(request.userId(), request.mandalaId());
        boolean sessionRegistered = mandalaProgressRepository.findByUserIdAndMandalaId(request.userId(), request.mandalaId())
                .map(progress -> progress.isSessionRegistered())
                .orElse(false);
        return mapper.toResponse(sessionRegistered);
    }

    private void validateAvailableMandala(Long userId, String mandalaId) {
        boolean available = listAvailableMandalasUseCase.execute(userId).stream()
                .anyMatch(availableMandala -> availableMandala.getMandala().getId().equals(mandalaId));
        if (!available) {
            throw new ResourceNotFoundException("Mandala no disponible");
        }
    }
}
