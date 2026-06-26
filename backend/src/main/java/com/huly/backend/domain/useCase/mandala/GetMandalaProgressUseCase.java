package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.dto.mandala.GetMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaProgressResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.mandala.GetMandalaProgressMapper;
import com.huly.backend.domain.model.mandala.MandalaProgress;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetMandalaProgressUseCase {

    private final MandalaProgressRepository mandalaProgressRepository;
    private final ListAvailableMandalasUseCase listAvailableMandalasUseCase;
    private final GetMandalaProgressMapper mapper;

    public GetMandalaProgressResponse execute(GetMandalaProgressRequest request) {
        validateAvailable(request.userId(), request.mandalaId());
        return mapper.toResponse(
                mandalaProgressRepository.findByUserIdAndMandalaId(request.userId(), request.mandalaId())
                        .map(MandalaProgress::getPaintBlob));
    }

    private void validateAvailable(Long userId, String mandalaId) {
        boolean available = listAvailableMandalasUseCase.execute(userId).stream()
                .anyMatch(availableMandala -> availableMandala.getMandala().getId().equals(mandalaId));
        if (!available) {
            throw new ResourceNotFoundException("Mandala no disponible");
        }
    }
}
