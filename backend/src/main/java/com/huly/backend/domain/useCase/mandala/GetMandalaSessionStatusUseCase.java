package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.dto.mandala.GetMandalaSessionStatusRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaSessionStatusResponse;
import com.huly.backend.domain.mapper.mandala.GetMandalaSessionStatusMapper;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import com.huly.backend.domain.service.mandala.MandalaService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetMandalaSessionStatusUseCase {

    private final MandalaProgressRepository mandalaProgressRepository;
    private final MandalaService mandalaService;
    private final GetMandalaSessionStatusMapper mapper;

    public GetMandalaSessionStatusResponse execute(GetMandalaSessionStatusRequest request) {
        mandalaService.validateMandalaAvailability(request.userId(), request.mandalaId());
        boolean sessionRegistered = mandalaProgressRepository.findByUserIdAndMandalaId(request.userId(), request.mandalaId())
                .map(progress -> progress.isSessionRegistered())
                .orElse(false);
        return mapper.toResponse(sessionRegistered);
    }
}
