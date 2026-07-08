package com.huly.backend.domain.model.mandala;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MandalaProgress {
    private final Long userId;
    private final String mandalaId;
    private final byte[] paintBlob;
    private final boolean sessionRegistered;
}
