package com.huly.backend.domain.useCase.mandala;

import com.huly.backend.domain.dto.mandala.SaveMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.SaveMandalaProgressResponse;
import com.huly.backend.domain.mapper.mandala.SaveMandalaProgressMapper;
import com.huly.backend.domain.repository.mandala.MandalaProgressRepository;
import com.huly.backend.domain.service.mandala.MandalaService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveMandalaProgressUseCase {

    private final MandalaProgressRepository mandalaProgressRepository;
    private final MandalaService mandalaService;
    private final SaveMandalaProgressMapper mapper;

    public SaveMandalaProgressResponse execute(SaveMandalaProgressRequest request) {
        mandalaService.validateMandalaAvailability(request.userId(), request.mandalaId());
        mandalaProgressRepository.save(mapper.toModel(request));
        return mapper.toResponse();
    }
}
