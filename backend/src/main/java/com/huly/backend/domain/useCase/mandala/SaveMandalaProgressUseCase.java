package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.dto.mandala.SaveMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.SaveMandalaProgressResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.mandala.SaveMandalaProgressMapper;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveMandalaProgressUseCase {

    private final MandalaProgressRepository mandalaProgressRepository;
    private final ListAvailableMandalasUseCase listAvailableMandalasUseCase;
    private final SaveMandalaProgressMapper mapper;

    public SaveMandalaProgressResponse execute(SaveMandalaProgressRequest request) {
        validateAvailable(request.userId(), request.mandalaId());
        mandalaProgressRepository.save(mapper.toModel(request));
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
