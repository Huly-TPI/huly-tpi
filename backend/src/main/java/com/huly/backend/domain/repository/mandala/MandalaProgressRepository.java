package com.huly.backend.domain.repository.mandala;

import com.huly.backend.domain.model.mandala.MandalaProgress;
import java.util.Optional;

public interface MandalaProgressRepository {
    MandalaProgress save(MandalaProgress mandalaProgress);
    Optional<MandalaProgress> findByUserIdAndMandalaId(Long userId, String mandalaId);
    void deleteByUserIdAndMandalaId(Long userId, String mandalaId);
}
