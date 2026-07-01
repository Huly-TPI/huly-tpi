package com.huly.backend.domain.mapper.mandala;

import com.huly.backend.domain.dto.mandala.GetMandalaSessionStatusResponse;

public class GetMandalaSessionStatusMapper {

    public GetMandalaSessionStatusResponse toResponse(boolean sessionRegistered) {
        return new GetMandalaSessionStatusResponse(sessionRegistered);
    }
}
