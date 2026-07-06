package com.huly.backend.domain.service.pending;

import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.port.pending.MentalLoadEstimate;
import com.huly.backend.domain.port.pending.MentalLoadEstimationInput;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeuristicMentalLoadEstimatorTest {

    private final HeuristicMentalLoadEstimator estimator = new HeuristicMentalLoadEstimator();

    @Test
    void estimateFromSignals_shouldClampScoreToZeroOneRange() {
        MentalLoadEstimationInput input = new MentalLoadEstimationInput(
                "Tarea", "desc", null, 0, EstimatedDuration.FULL_DAY, null, 20);

        MentalLoadEstimate estimate = estimator.estimateFromSignals(MentalLoadBucket.HIGH, input);

        assertThat(estimate.score()).isBetween(0.0, 1.0);
        assertThat(estimate.bucket()).isEqualTo(MentalLoadBucket.HIGH);
    }

    @Test
    void estimateFromSignals_highBucketWithUrgentDueDate_shouldScoreHigherThanLowBucketNoSignals() {
        MentalLoadEstimationInput urgent = new MentalLoadEstimationInput(
                "Tarea urgente", "desc", null, 0, EstimatedDuration.FULL_DAY, null, 5);
        MentalLoadEstimationInput light = new MentalLoadEstimationInput(
                "Tarea liviana", null, null, null, EstimatedDuration.FIFTEEN_MIN, null, 0);

        MentalLoadEstimate highEstimate = estimator.estimateFromSignals(MentalLoadBucket.HIGH, urgent);
        MentalLoadEstimate lowEstimate = estimator.estimateFromSignals(MentalLoadBucket.LOW, light);

        assertThat(highEstimate.score()).isGreaterThan(lowEstimate.score());
    }

    @Test
    void estimateWithoutAi_shouldReturnHigh_whenDueTomorrowOrSooner() {
        MentalLoadEstimationInput input = new MentalLoadEstimationInput(
                "Entrega", "desc", null, 1, null, null, 0);

        MentalLoadEstimate estimate = estimator.estimateWithoutAi(input);

        assertThat(estimate.bucket()).isEqualTo(MentalLoadBucket.HIGH);
    }

    @Test
    void estimateWithoutAi_shouldReturnLow_whenNoSignalsPresent() {
        MentalLoadEstimationInput input = new MentalLoadEstimationInput(
                "Tarea simple", null, null, null, null, null, 0);

        MentalLoadEstimate estimate = estimator.estimateWithoutAi(input);

        assertThat(estimate.bucket()).isEqualTo(MentalLoadBucket.LOW);
    }
}
