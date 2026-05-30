package com.huly.backend.domain.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

public class BreathingTechnique { 
    private Long id;
    private String name;
    private String description;
    private int inhaleSeconds;
    private int holdSeconds;
    private int exhaleSeconds;
    private int roundsInterval;
    private int rounds;
}