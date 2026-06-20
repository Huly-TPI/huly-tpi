package com.huly.backend.domain.model.activity;
import com.huly.backend.domain.model.enums.ActivityType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Activity {
    private Long id;
    private ActivityType type;
    private double valenceMin;
    private double valenceMax;
    private double arousalMin;
    private double arousalMax;
    private double dominanceMin;
    private double dominanceMax;
    private double effectValence;
    private double effectArousal;
    private double effectDominance;
}
