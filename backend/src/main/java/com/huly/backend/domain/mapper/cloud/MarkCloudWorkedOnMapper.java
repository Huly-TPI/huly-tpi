package com.huly.backend.domain.mapper.cloud;

import com.huly.backend.domain.dto.cloud.MarkCloudWorkedOnResponse;

/**
 * Mapper de dominio para el caso de uso de marcar una nube como trabajada.
 */
public class MarkCloudWorkedOnMapper {

    public MarkCloudWorkedOnResponse toResponse(Long id) {
        return new MarkCloudWorkedOnResponse(id);
    }
}
