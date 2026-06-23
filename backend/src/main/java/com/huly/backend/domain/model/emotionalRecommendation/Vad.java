package com.huly.backend.domain.model.emotionalRecommendation;

/**
 * Represents an emotional state using valence, arousal and dominance dimensions.
 *
 * @param valence emotional pleasantness, from {@value #MIN_VALUE} to {@value #MAX_VALUE}
 * @param arousal emotional activation, from {@value #MIN_VALUE} to {@value #MAX_VALUE}
 * @param dominance perceived control, from {@value #MIN_VALUE} to {@value #MAX_VALUE}
 */
public record Vad(
        double valence,
        double arousal,
        double dominance
) {

    /** Minimum supported value for each VAD dimension. */
    public static final double MIN_VALUE = -1.0;

    /** Maximum supported value for each VAD dimension. */
    public static final double MAX_VALUE = 1.0;

    /**
     * Indicates whether all VAD dimensions are finite and inside the supported range.
     *
     * @return {@code true} when every dimension is valid
     */
    public boolean isValid() {
        return isValidDimension(valence)
                && isValidDimension(arousal)
                && isValidDimension(dominance);
    }

    /**
     * Indicates whether a VAD dimension is finite and inside the supported range.
     *
     * @param value dimension value
     * @return {@code true} when the value is valid
     */
    public static boolean isValidDimension(double value) {
        return Double.isFinite(value) && value >= MIN_VALUE && value <= MAX_VALUE;
    }

    /**
     * Limits a VAD dimension to the supported range.
     *
     * @param value dimension value
     * @return the bounded value
     */
    public static double clampDimension(double value) {
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
    }
}
