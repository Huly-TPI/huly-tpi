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
public class GeneratedChallengeEmbeddable {

    @Column(name = "generated_challenge_title", length = 255)
    private String title;

    @Column(name = "generated_challenge_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "challenge_decision", length = 50)
    private String decision;
}
