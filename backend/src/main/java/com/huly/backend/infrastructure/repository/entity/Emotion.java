package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.EmotionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "emotion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat_message")
    private ChatMessage chatMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "emotion_detected", length = 80)
    private EmotionType emotionDetected;

}
