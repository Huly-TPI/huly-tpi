package com.huly.backend.presentation.dto.dashboard;

import java.util.List;

/**
 * DTO de salida para los datos históricos de bienestar general.
 *
 * @param points lista de puntajes de bienestar (0–100) ordenados cronológicamente
 * @param labels etiquetas de cada punto (ej. "Lun", "Mar", etc.)
 */
public record WellbeingResponse(
        List<Integer> points,
        List<String> labels
) {}
