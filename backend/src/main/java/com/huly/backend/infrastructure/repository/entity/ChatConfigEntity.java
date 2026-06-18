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

    @Column(name = "preferred_name_question_enabled", nullable = false)
    private Boolean preferredNameQuestionEnabled;

    @Column(name = "communication_style_question_enabled", nullable = false)
    private Boolean communicationStyleQuestionEnabled;
}
