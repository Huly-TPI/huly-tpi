package com.huly.backend.domain.model.pending;

import com.huly.backend.domain.model.enums.PendingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PendingTaskTest {

    @Test
    @DisplayName("Marca la tarea como completada y registra la marca de tiempo correspondiente")
    void completeShouldMarkTaskAsCompleted() {
        PendingTask task = pendingTask();
        Instant now = Instant.now();

        performComplete(task, now);

        thenStatusIs(task, PendingStatus.COMPLETED);
        thenCompletedAtIs(task, now);
    }

    @Test
    @DisplayName("Lanza excepción si se intenta completar una tarea que ya está completada")
    void completeShouldThrowWhenAlreadyCompleted() {
        PendingTask task = pendingTask();
        givenTaskAlreadyCompleted(task);

        assertThatThrownBy(() -> performComplete(task, Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Asigna la posición, una rotación del tablero y la marca de tiempo en el primer pin de la tarea")
    void applyPositionShouldAssignRotationAndPinnedAtOnFirstPin() {
        PendingTask task = pendingTask();
        Instant now = Instant.now();

        performApplyPosition(task, 50.0, 60.0, 3.5, now);

        thenTaskIsPlaced(task, true);
        thenTaskCoordinatesAre(task, 50.0, 60.0, 3.5);
        thenPinnedAtIs(task, now);
    }

    @Test
    @DisplayName("Mantiene la rotación original y actualiza únicamente las coordenadas en pines sucesivos (re-pin)")
    void applyPositionShouldKeepExistingRotationOnRepin() {
        PendingTask task = pendingTask();
        Instant firstPinTime = Instant.now();
        givenTaskAlreadyPinned(task, 10.0, 10.0, 2.0, firstPinTime);

        performApplyPosition(task, 80.0, 20.0, 99.0, Instant.now());

        thenTaskCoordinatesAre(task, 80.0, 20.0, 2.0);
        thenPinnedAtIs(task, firstPinTime);
    }

    @Test
    @DisplayName("Lanza excepción si las coordenadas ingresadas están fuera de los límites de pantalla de 0 a 100")
    void applyPositionShouldThrowWhenOutOfRange() {
        PendingTask task = pendingTask();

        assertThatThrownBy(() -> performApplyPosition(task, -1.0, 50.0, 1.0, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> performApplyPosition(task, 50.0, 101.0, 1.0, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- arrange ---

    private void givenTaskAlreadyCompleted(PendingTask task) {
        task.complete(Instant.now());
    }

    private void givenTaskAlreadyPinned(PendingTask task, double x, double y, double rot, Instant time) {
        task.applyPosition(x, y, rot, time);
    }

    // --- act ---

    private void performComplete(PendingTask task, Instant now) {
        task.complete(now);
    }

    private void performApplyPosition(PendingTask task, double x, double y, double rot, Instant now) {
        task.applyPosition(x, y, rot, now);
    }

    // --- assert ---

    private void thenStatusIs(PendingTask task, PendingStatus expected) {
        assertThat(task.getStatus()).isEqualTo(expected);
    }

    private void thenCompletedAtIs(PendingTask task, Instant expected) {
        assertThat(task.getCompletedAt()).isEqualTo(expected);
    }

    private void thenTaskIsPlaced(PendingTask task, boolean expected) {
        assertThat(task.isPlaced()).isEqualTo(expected);
    }

    private void thenTaskCoordinatesAre(PendingTask task, double x, double y, double rot) {
        assertThat(task.getPositionX()).isEqualTo(x);
        assertThat(task.getPositionY()).isEqualTo(y);
        assertThat(task.getRotationDeg()).isEqualTo(rot);
    }

    private void thenPinnedAtIs(PendingTask task, Instant expected) {
        assertThat(task.getPinnedAt()).isEqualTo(expected);
    }

    // --- helpers ---

    private PendingTask pendingTask() {
        return PendingTask.builder()
                .id(1L)
                .userId(10L)
                .title("Lavar los platos")
                .status(PendingStatus.PENDING)
                .build();
    }
}
