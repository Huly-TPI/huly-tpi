package com.huly.backend.domain.model.mandala;

import com.huly.backend.domain.model.enums.MandalaAccessType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Mandala {
    private final String id;
    private final String title;
    private final String description;
    private final String assetKey;
    private final int displayOrder;
    private final boolean active;
    private final MandalaAccessType accessType;
    private final Integer priceCoins;
}
