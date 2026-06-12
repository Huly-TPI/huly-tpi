package com.huly.backend.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VadTest {

    @Test
    void isValid_shouldAcceptBoundaryValues() {
        assertThat(new Vad(-1.0, 0.0, 1.0).isValid()).isTrue();
    }

    @Test
    void isValid_shouldRejectNonFiniteOrOutOfRangeValues() {
        assertThat(new Vad(Double.NaN, 0.0, 0.0).isValid()).isFalse();
        assertThat(new Vad(0.0, 1.1, 0.0).isValid()).isFalse();
    }

    @Test
    void clampDimension_shouldLimitValuesToVadRange() {
        assertThat(Vad.clampDimension(-1.5)).isEqualTo(-1.0);
        assertThat(Vad.clampDimension(1.5)).isEqualTo(1.0);
        assertThat(Vad.clampDimension(0.25)).isEqualTo(0.25);
    }
}
