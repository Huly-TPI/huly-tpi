package com.huly.backend.domain.dto.mandala;

import java.util.Optional;

/**
 * Respuesta de dominio con el progreso de pintura de un mandala.
 * El contenido es opcional: estara vacio si no existe progreso almacenado.
 */
public record GetMandalaProgressResponse(Optional<byte[]> paintBlob) {
}
