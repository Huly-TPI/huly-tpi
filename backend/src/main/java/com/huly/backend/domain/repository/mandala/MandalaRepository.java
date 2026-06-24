package com.huly.backend.domain.repository.mandala;

import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.domain.model.mandala.Mandala;

import java.util.List;
import java.util.Optional;

public interface MandalaRepository {
    List<Mandala> findAllActiveOrderByDisplayOrder();
    List<Mandala> findAllActiveByAccessTypeOrderByDisplayOrder(MandalaAccessType accessType);
    Optional<Mandala> findById(String id);
}
