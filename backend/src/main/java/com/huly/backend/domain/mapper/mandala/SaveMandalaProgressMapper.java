package com.huly.backend.domain.mapper.mandala;

import com.huly.backend.domain.dto.mandala.SaveMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.SaveMandalaProgressResponse;
import com.huly.backend.domain.model.mandala.MandalaProgress;

/**
 * Mapper de dominio para el caso de uso de guardado de progreso de mandala.
 */
public class SaveMandalaProgressMapper {

    public MandalaProgress toModel(SaveMandalaProgressRequest request) {
        return MandalaProgress.builder()
                .userId(request.userId())
                .mandalaId(request.mandalaId())
                .paintBlob(request.paintBlob())
                .build();
    }

    public SaveMandalaProgressResponse toResponse() {
        return new SaveMandalaProgressResponse();
    }
}
