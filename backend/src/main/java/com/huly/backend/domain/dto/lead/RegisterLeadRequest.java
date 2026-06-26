package com.huly.backend.domain.dto.lead;

import com.huly.backend.domain.model.enums.SourceAction;

/**
 * Pedido de dominio para registrar un lead.
 *
 * @param email        email del lead.
 * @param nickname     nombre de usuario del lead.
 * @param sourceAction accion de origen desde la que se registro el lead.
 */
public record RegisterLeadRequest(String email, String nickname, SourceAction sourceAction) {
}
