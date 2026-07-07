package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.ActivityType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "activity")
public class ActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Column(name = "valence_min") 
    private double valenceMin;

    @Column(name = "valence_max") 
    private double valenceMax;

    @Column(name = "arousal_min") 
    private double arousalMin;

    @Column(name = "arousal_max") 
    private double arousalMax;

    @Column(name = "dominance_min") 
    private double dominanceMin;

    @Column(name = "dominance_max") 
    private double dominanceMax;

    @Column(name = "effect_valence") 
    private double effectValence;

    @Column(name = "effect_arousal") 
    private double effectArousal;
    
    @Column(name = "effect_dominance") 
    private double effectDominance;

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "goal_keywords", columnDefinition = "TEXT")
    private String goalKeywords;

    @Column(name = "route_path")
    private String routePath;
   
}