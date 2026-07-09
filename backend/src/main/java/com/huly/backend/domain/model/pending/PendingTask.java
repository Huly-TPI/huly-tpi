package com.huly.backend.domain.model.pending;

import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.model.enums.PendingCategory;
import com.huly.backend.domain.model.enums.PendingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingTask {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private EstimatedDuration estimatedDuration;
    private PendingCategory category;
    private PendingStatus status;
    private Double mentalLoadScore;
    private MentalLoadBucket mentalLoadBucket;
    private Double positionX;
    private Double positionY;
    private Double rotationDeg;
    private Instant pinnedAt;
    @Builder.Default
    private List<PendingSubtask> subtasks = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    public boolean isPending() {
        return status == PendingStatus.PENDING;
    }

    public boolean isPlaced() {
        return pinnedAt != null;
    }

    public void complete(Instant now) {
        if (status == PendingStatus.COMPLETED) {
            throw new IllegalStateException("La tarea ya está completada");
        }
        this.status = PendingStatus.COMPLETED;
        this.completedAt = now;
        this.updatedAt = now;
    }

    public void applyPosition(double x, double y, Double assignedRotationIfFirstPin, Instant now) {
        if (x < 0 || x > 100 || y < 0 || y > 100) {
            throw new IllegalArgumentException("La posición debe estar entre 0 y 100");
        }
        boolean firstPin = this.pinnedAt == null;
        this.positionX = x;
        this.positionY = y;
        if (firstPin) {
            this.rotationDeg = assignedRotationIfFirstPin;
            this.pinnedAt = now;
        }
        this.updatedAt = now;
    }
}
