package com.huly.backend.domain.mapper.mandala;

import com.huly.backend.domain.dto.mandala.ListAvailableMandalasResponse;
import com.huly.backend.domain.dto.mandala.MandalaItem;
import com.huly.backend.domain.model.mandala.AvailableMandala;
import org.springframework.data.domain.Page;

/**
 * Mapper de dominio para el caso de uso de listado de mandalas disponibles.
 */
public class ListAvailableMandalasMapper {

    public ListAvailableMandalasResponse toResponse(Page<AvailableMandala> page) {
        return new ListAvailableMandalasResponse(
                page.getContent().stream().map(this::toItem).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private MandalaItem toItem(AvailableMandala availableMandala) {
        return new MandalaItem(
                availableMandala.getMandala().getId(),
                availableMandala.getMandala().getTitle(),
                availableMandala.getMandala().getDescription(),
                availableMandala.getMandala().getAssetKey(),
                availableMandala.getMandala().getDisplayOrder(),
                availableMandala.getUnlockSource() != null ? availableMandala.getUnlockSource().name() : null,
                availableMandala.getMandala().getAccessType().name(),
                availableMandala.isLocked()
        );
    }
}
