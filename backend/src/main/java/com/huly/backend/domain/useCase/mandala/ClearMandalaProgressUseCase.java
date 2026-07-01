package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.dto.mandala.ClearMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.ClearMandalaProgressResponse;
import com.huly.backend.domain.mapper.mandala.ClearMandalaProgressMapper;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import com.huly.backend.domain.service.mandala.MandalaService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class ClearMandalaProgressUseCase {

    private final MandalaProgressRepository mandalaProgressRepository;
    private final MandalaService mandalaService;
    private final ClearMandalaProgressMapper mapper;

    @Transactional
    public ClearMandalaProgressResponse execute(ClearMandalaProgressRequest request) {
        mandalaService.validateMandalaAvailability(request.userId(), request.mandalaId());
        mandalaProgressRepository.deleteByUserIdAndMandalaId(request.userId(), request.mandalaId());
        return mapper.toResponse();
    }
}
