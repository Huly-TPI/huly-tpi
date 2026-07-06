package com.huly.backend.domain.port.pending;

public interface MentalLoadEstimationPort {
    MentalLoadEstimate estimate(MentalLoadEstimationInput input);
}
