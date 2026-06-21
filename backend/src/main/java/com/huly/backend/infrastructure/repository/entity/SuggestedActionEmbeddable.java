package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestedActionEmbeddable {

    @Column(name = "suggested_action_type", length = 50)
    private String type;

    @Column(name = "suggested_action_activity_id")
    private Long activityId;

    @Column(name = "suggested_action_title", length = 255)
    private String title;

    @Column(name = "suggested_action_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "suggested_action_url", length = 500)
    private String actionUrl;

    @Column(name = "suggested_action_emotional_event_id")
    private Long emotionalEventId;

    @Column(name = "suggested_action_decision", length = 50)
    private String decision;
}
