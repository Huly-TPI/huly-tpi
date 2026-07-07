package com.huly.backend.domain.service.pending;

import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.port.pending.MentalLoadEstimate;
import com.huly.backend.domain.port.pending.MentalLoadEstimationInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeuristicMentalLoadEstimatorTest {

    private final HeuristicMentalLoadEstimator estimator = new HeuristicMentalLoadEstimator();

    @Test
    @DisplayName("Acota el puntaje calculado de carga mental en el rango cerrado de 0.0 a 1.0")
    void estimateFromSignalsShouldClampScoreToZeroOneRange() {
        MentalLoadEstimationInput input = new MentalLoadEstimationInput(
                "Tarea", "desc", null, 0, EstimatedDuration.FULL_DAY, null, 20);

        MentalLoadEstimate estimate = estimateFromSignals(MentalLoadBucket.HIGH, input);

        thenScoreIsBetweenZeroAndOne(estimate);
        thenBucketIs(estimate, MentalLoadBucket.HIGH);
    }

    @Test
    @DisplayName("Asigna mayor puntaje de carga a una tarea en bucket alto y fecha urgente que a una sin señales en bucket bajo")
    void estimateFromSignalsHighBucketWithUrgentDueDateShouldScoreHigherThanLowBucketNoSignals() {
        MentalLoadEstimationInput urgent = new MentalLoadEstimationInput(
                "Tarea urgente", "desc", null, 0, EstimatedDuration.FULL_DAY, null, 5);
        MentalLoadEstimationInput light = new MentalLoadEstimationInput(
                "Tarea liviana", null, null, null, EstimatedDuration.FIFTEEN_MIN, null, 0);

        MentalLoadEstimate highEstimate = estimateFromSignals(MentalLoadBucket.HIGH, urgent);
        MentalLoadEstimate lowEstimate = estimateFromSignals(MentalLoadBucket.LOW, light);

        thenHighEstimateIsGreaterThanLow(highEstimate, lowEstimate);
    }

    @Test
    @DisplayName("Asigna la tarea al bucket HIGH si la fecha límite es mañana o antes en estimación manual")
    void estimateWithoutAiShouldReturnHighWhenDueTomorrowOrSooner() {
        MentalLoadEstimationInput input = new MentalLoadEstimationInput(
                "Entrega", "desc", null, 1, null, null, 0);

        MentalLoadEstimate estimate = estimateWithoutAi(input);

        thenBucketIs(estimate, MentalLoadBucket.HIGH);
    }

    @Test
    @DisplayName("Asigna la tarea al bucket LOW si no hay señales de urgencia presentes en estimación manual")
    void estimateWithoutAiShouldReturnLowWhenNoSignalsPresent() {
        MentalLoadEstimationInput input = new MentalLoadEstimationInput(
                "Tarea simple", null, null, null, null, null, 0);

        MentalLoadEstimate estimate = estimateWithoutAi(input);

        thenBucketIs(estimate, MentalLoadBucket.LOW);
    }

    // --- act ---

    private MentalLoadEstimate estimateFromSignals(MentalLoadBucket bucket, MentalLoadEstimationInput input) {
        return estimator.estimateFromSignals(bucket, input);
    }

    private MentalLoadEstimate estimateWithoutAi(MentalLoadEstimationInput input) {
        return estimator.estimateWithoutAi(input);
    }

    // --- assert ---

    private void thenScoreIsBetweenZeroAndOne(MentalLoadEstimate estimate) {
        assertThat(estimate.score()).isBetween(0.0, 1.0);
    }

    private void thenBucketIs(MentalLoadEstimate estimate, MentalLoadBucket expected) {
        assertThat(estimate.bucket()).isEqualTo(expected);
    }

    private void thenHighEstimateIsGreaterThanLow(MentalLoadEstimate high, MentalLoadEstimate low) {
        assertThat(high.score()).isGreaterThan(low.score());
    }
}
