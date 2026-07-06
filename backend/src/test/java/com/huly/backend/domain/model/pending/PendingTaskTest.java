package com.huly.backend.domain.model.pending;

import com.huly.backend.domain.model.enums.PendingStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PendingTaskTest {

    @Test
    void complete_shouldMarkTaskAsCompleted() {
        PendingTask task = pendingTask();
        Instant now = Instant.now();

        task.complete(now);

        assertThat(task.getStatus()).isEqualTo(PendingStatus.COMPLETED);
        assertThat(task.getCompletedAt()).isEqualTo(now);
    }

    @Test
    void complete_shouldThrow_whenAlreadyCompleted() {
        PendingTask task = pendingTask();
        task.complete(Instant.now());

        assertThatThrownBy(() -> task.complete(Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void applyPosition_shouldAssignRotationAndPinnedAt_onFirstPin() {
        PendingTask task = pendingTask();
        Instant now = Instant.now();

        task.applyPosition(50.0, 60.0, 3.5, now);

        assertThat(task.isPlaced()).isTrue();
        assertThat(task.getPositionX()).isEqualTo(50.0);
        assertThat(task.getPositionY()).isEqualTo(60.0);
        assertThat(task.getRotationDeg()).isEqualTo(3.5);
        assertThat(task.getPinnedAt()).isEqualTo(now);
    }

    @Test
    void applyPosition_shouldKeepExistingRotation_onRepin() {
        PendingTask task = pendingTask();
        Instant firstPinTime = Instant.now();
        task.applyPosition(10.0, 10.0, 2.0, firstPinTime);

        task.applyPosition(80.0, 20.0, 99.0, Instant.now());

        assertThat(task.getRotationDeg()).isEqualTo(2.0);
        assertThat(task.getPositionX()).isEqualTo(80.0);
        assertThat(task.getPositionY()).isEqualTo(20.0);
        assertThat(task.getPinnedAt()).isEqualTo(firstPinTime);
    }

    @Test
    void applyPosition_shouldThrow_whenOutOfRange() {
        PendingTask task = pendingTask();

        assertThatThrownBy(() -> task.applyPosition(-1.0, 50.0, 1.0, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> task.applyPosition(50.0, 101.0, 1.0, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private PendingTask pendingTask() {
        return PendingTask.builder()
                .id(1L)
                .userId(10L)
                .title("Lavar los platos")
                .status(PendingStatus.PENDING)
                .build();
    }
}
