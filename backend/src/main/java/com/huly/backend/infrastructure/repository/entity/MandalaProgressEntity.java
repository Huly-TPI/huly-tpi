package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "mandala_progress")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MandalaProgressEntity.MandalaProgressId.class)
public class MandalaProgressEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "mandala_id")
    private String mandalaId;

    @Column(name = "paint_blob", nullable = false)
    private byte[] paintBlob;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MandalaProgressId implements Serializable {
        private Long userId;
        private String mandalaId;
    }
}
