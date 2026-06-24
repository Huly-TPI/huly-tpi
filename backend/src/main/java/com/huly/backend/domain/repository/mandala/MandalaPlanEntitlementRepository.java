package com.huly.backend.domain.repository.mandala;

import java.util.List;

public interface MandalaPlanEntitlementRepository {
    List<String> findMandalaIdsByPlanCode(String planCode);
}
