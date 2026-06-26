package com.huly.backend.domain.dto.cloud;

import java.util.List;

/**
 * Respuesta de dominio con el listado de pensamientos de nube de un usuario.
 */
public record ListCloudThoughtsResponse(List<CloudThoughtItem> thoughts) {
}
