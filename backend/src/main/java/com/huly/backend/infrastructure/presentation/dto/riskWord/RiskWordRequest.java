package com.huly.backend.infrastructure.presentation.dto.riskWord;

import com.huly.backend.domain.model.enums.RiskSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear o actualizar una palabra de riesgo.
 * Se utiliza tanto en el endpoint de creación ({@code POST /api/risk-words})
 * como en el de actualización ({@code PUT /api/risk-words/{id}}).
 *
 * @param word        palabra o frase de riesgo; obligatoria, máximo 200 caracteres
 * @param description descripción opcional del contexto de riesgo; máximo 1000 caracteres
 * @param severity    nivel de severidad del riesgo; obligatorio
 */
public record RiskWordRequest(
        @NotBlank(message = "La palabra es obligatoria")
        @Size(max = 200, message = "La palabra no puede superar los 200 caracteres")
        String word,

        @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
        String description,

        @NotNull(message = "La severidad es obligatoria")
        RiskSeverity severity
) {}
