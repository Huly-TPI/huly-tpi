package com.huly.backend.infrastructure.presentation.dto.admin.activities;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateActivityConfigRequest {
    private double valenceMin;
    private double valenceMax;
    private double arousalMin;
    private double arousalMax;
    private double dominanceMin;
    private double dominanceMax;
    private double effectValence;
    private double effectArousal;
    private double effectDominance;

    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    private String goalKeywords;

    @NotBlank(message = "La ruta de navegación es obligatoria")
    private String routePath;
}
