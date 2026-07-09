package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "pending_subtask")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingSubtaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private PendingTaskEntity task;

    @Column(name = "text", nullable = false, length = 200)
    private String text;

    @Column(name = "done", nullable = false)
    private boolean done;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
