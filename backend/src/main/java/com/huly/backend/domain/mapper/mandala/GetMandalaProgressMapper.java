package com.huly.backend.domain.mapper.mandala;

import com.huly.backend.domain.dto.mandala.GetMandalaProgressResponse;

import java.util.Optional;

/**
 * Mapper de dominio para el caso de uso de obtencion de progreso de mandala.
 */
public class GetMandalaProgressMapper {

    public GetMandalaProgressResponse toResponse(Optional<byte[]> paintBlob) {
        return new GetMandalaProgressResponse(paintBlob);
    }
}
