package com.huly.backend.domain.mapper.user;

import com.huly.backend.domain.dto.user.GetUserCoinsResponse;

/**
 * Mapper de dominio para el caso de uso de obtencion de coins del usuario.
 */
public class GetUserCoinsMapper {

    public GetUserCoinsResponse toResponse(int coins) {
        return new GetUserCoinsResponse(coins);
    }
}
