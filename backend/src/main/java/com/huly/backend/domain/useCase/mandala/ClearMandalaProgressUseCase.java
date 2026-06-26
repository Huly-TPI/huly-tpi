package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.dto.mandala.ClearMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.ClearMandalaProgressResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.mandala.ClearMandalaProgressMapper;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class ClearMandalaProgressUseCase {

    private final MandalaProgressRepository mandalaProgressRepository;
    private final ListAvailableMandalasUseCase listAvailableMandalasUseCase;
    private final ClearMandalaProgressMapper mapper;

    @Transactional
    public ClearMandalaProgressResponse execute(ClearMandalaProgressRequest request) {
        validateAvailable(request.userId(), request.mandalaId());
        mandalaProgressRepository.deleteByUserIdAndMandalaId(request.userId(), request.mandalaId());
        return mapper.toResponse();
    }

    private void validateAvailable(Long userId, String mandalaId) {
        boolean available = listAvailableMandalasUseCase.execute(userId).stream()
                .anyMatch(availableMandala -> availableMandala.getMandala().getId().equals(mandalaId));
        if (!available) {
            throw new ResourceNotFoundException("Mandala no disponible");
        }
    }
}
