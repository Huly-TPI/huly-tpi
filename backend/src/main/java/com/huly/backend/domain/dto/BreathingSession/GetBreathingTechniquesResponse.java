package com.huly.backend.domain.dto.BreathingSession;

import java.util.List;

/**
 * Respuesta de dominio con el listado de tecnicas de respiracion disponibles.
 */
public record GetBreathingTechniquesResponse(List<BreathingTechniqueItem> techniques) {
}
