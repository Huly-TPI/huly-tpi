package com.huly.backend.domain.model.pending;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingSubtask {
    private Long id;
    private Long taskId;
    private String text;
    private boolean done;
    private int position;
    private Instant createdAt;

    public void toggle() {
        this.done = !this.done;
    }
}
