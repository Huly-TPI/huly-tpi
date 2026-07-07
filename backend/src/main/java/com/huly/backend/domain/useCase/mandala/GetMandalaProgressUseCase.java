package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.dto.mandala.GetMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaProgressResponse;
import com.huly.backend.domain.mapper.mandala.GetMandalaProgressMapper;
import com.huly.backend.domain.model.mandala.MandalaProgress;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import com.huly.backend.domain.service.mandala.MandalaService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetMandalaProgressUseCase {

    private final MandalaProgressRepository mandalaProgressRepository;
    private final MandalaService mandalaService;
    private final GetMandalaProgressMapper mapper;

    public GetMandalaProgressResponse execute(GetMandalaProgressRequest request) {
        mandalaService.validateMandalaAvailability(request.userId(), request.mandalaId());
        return mapper.toResponse(
                mandalaProgressRepository.findByUserIdAndMandalaId(request.userId(), request.mandalaId())
                        .map(MandalaProgress::getPaintBlob));
    }
}
