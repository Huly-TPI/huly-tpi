package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.EmotionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emotion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat_message")
    private ChatMessageEntity chatMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "emotion_detected", length = 80)
    private EmotionType emotionDetected;

}
