package com.huly.backend.domain.service.pending;

import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.port.pending.MentalLoadEstimate;
import com.huly.backend.domain.port.pending.MentalLoadEstimationInput;
import org.springframework.stereotype.Service;

@Service
public class HeuristicMentalLoadEstimator {

    private static final double LOW_BASE = 0.2;
    private static final double MEDIUM_BASE = 0.5;
    private static final double HIGH_BASE = 0.8;

    private static final int SUBTASK_THRESHOLD = 3;
    private static final double SUBTASK_STEP = 0.02;
    private static final double SUBTASK_ADJUSTMENT_CAP = 0.1;

    public MentalLoadEstimate estimateFromSignals(MentalLoadBucket bucket, MentalLoadEstimationInput input) {
        double score = bucketBase(bucket)
                + dueDateProximityAdjustment(input.daysUntilDue())
                + durationAdjustment(input.estimatedDuration())
                + subtaskCountAdjustment(input.subtaskCount());
        return new MentalLoadEstimate(clamp(score), bucket);
    }

    public MentalLoadEstimate estimateWithoutAi(MentalLoadEstimationInput input) {
        MentalLoadBucket bucket = heuristicBucket(input);
        return estimateFromSignals(bucket, input);
    }

    private MentalLoadBucket heuristicBucket(MentalLoadEstimationInput input) {
        boolean dueSoon = input.daysUntilDue() != null && input.daysUntilDue() <= 1;
        boolean fullDay = input.estimatedDuration() == EstimatedDuration.FULL_DAY;
        boolean manySubtasks = input.subtaskCount() >= 5;
        if (dueSoon || fullDay || manySubtasks) {
            return MentalLoadBucket.HIGH;
        }

        boolean noDueDate = input.dueDate() == null;
        boolean lightDuration = input.estimatedDuration() == null || input.estimatedDuration() == EstimatedDuration.FIFTEEN_MIN;
        boolean fewSubtasks = input.subtaskCount() <= 1;
        if (noDueDate && lightDuration && fewSubtasks) {
            return MentalLoadBucket.LOW;
        }

        return MentalLoadBucket.MEDIUM;
    }

    private double bucketBase(MentalLoadBucket bucket) {
        if (bucket == null) {
            return MEDIUM_BASE;
        }
        return switch (bucket) {
            case LOW -> LOW_BASE;
            case MEDIUM -> MEDIUM_BASE;
            case HIGH -> HIGH_BASE;
        };
    }

    private double dueDateProximityAdjustment(Integer daysUntilDue) {
        if (daysUntilDue == null) {
            return 0.0;
        }
        if (daysUntilDue <= 1) {
            return 0.1;
        }
        if (daysUntilDue <= 3) {
            return 0.05;
        }
        return 0.0;
    }

    private double durationAdjustment(EstimatedDuration duration) {
        if (duration == null) {
            return 0.0;
        }
        return switch (duration) {
            case FULL_DAY -> 0.1;
            case HALF_DAY -> 0.05;
            case ONE_HOUR -> 0.0;
            case FIFTEEN_MIN -> -0.05;
        };
    }

    private double subtaskCountAdjustment(int subtaskCount) {
        if (subtaskCount <= SUBTASK_THRESHOLD) {
            return 0.0;
        }
        double adjustment = (subtaskCount - SUBTASK_THRESHOLD) * SUBTASK_STEP;
        return Math.min(adjustment, SUBTASK_ADJUSTMENT_CAP);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
