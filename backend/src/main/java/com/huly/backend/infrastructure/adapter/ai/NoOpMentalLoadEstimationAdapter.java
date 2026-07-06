package com.huly.backend.infrastructure.adapter.ai;

import com.huly.backend.domain.port.pending.MentalLoadEstimate;
import com.huly.backend.domain.port.pending.MentalLoadEstimationInput;
import com.huly.backend.domain.port.pending.MentalLoadEstimationPort;
import com.huly.backend.domain.service.pending.HeuristicMentalLoadEstimator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoOpMentalLoadEstimationAdapter implements MentalLoadEstimationPort {

    private final HeuristicMentalLoadEstimator heuristicEstimator;

    @Override
    public MentalLoadEstimate estimate(MentalLoadEstimationInput input) {
        return heuristicEstimator.estimateWithoutAi(input);
    }
}
