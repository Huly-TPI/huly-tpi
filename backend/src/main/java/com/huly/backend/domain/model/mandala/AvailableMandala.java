package com.huly.backend.domain.model.mandala;

import com.huly.backend.domain.model.enums.MandalaUnlockSource;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailableMandala {
    private final Mandala mandala;
    private final MandalaUnlockSource unlockSource;
    private final boolean locked;
}
