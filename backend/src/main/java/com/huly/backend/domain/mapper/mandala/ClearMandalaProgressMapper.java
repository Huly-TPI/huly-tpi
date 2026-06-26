package com.huly.backend.domain.mapper.mandala;

import com.huly.backend.domain.dto.mandala.ClearMandalaProgressResponse;

/**
 * Mapper de dominio para el caso de uso de eliminacion de progreso de mandala.
 */
public class ClearMandalaProgressMapper {

    public ClearMandalaProgressResponse toResponse() {
        return new ClearMandalaProgressResponse();
    }
}
