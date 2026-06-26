package com.huly.backend.domain.dto.userGoal;

/**
 * Pedido de dominio para obtener las metas de un usuario paginadas.
 * El caso de uso resuelve por separado las metas completadas y las pendientes.
 *
 * @param userId usuario propietario de las metas.
 * @param page   numero de pagina (base 0).
 * @param size   cantidad de elementos por pagina.
 */
public record GetUserGoalsRequest(
        Long userId,
        int page,
        int size
) {
}
