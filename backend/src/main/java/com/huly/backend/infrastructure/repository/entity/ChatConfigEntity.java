package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "chat_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "risk_detection_enabled")
    private Boolean riskDetectionEnabled;

    @Column(name = "system_prompt")
    private String systemPrompt;

}
