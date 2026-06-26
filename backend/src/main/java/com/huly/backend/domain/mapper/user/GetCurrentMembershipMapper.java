package com.huly.backend.domain.mapper.user;

import com.huly.backend.domain.dto.user.GetCurrentMembershipResponse;
import com.huly.backend.domain.model.user.UserPlan;

import java.util.Optional;

/**
 * Mapper de dominio para el caso de uso de membresia vigente del usuario.
 */
public class GetCurrentMembershipMapper {

    public GetCurrentMembershipResponse toResponse(Optional<UserPlan> plan) {
        return plan
                .map(p -> new GetCurrentMembershipResponse(
                        true,
                        p.getPlanCode(),
                        p.getProductId(),
                        p.getExpiresAt()))
                .orElseGet(GetCurrentMembershipResponse::inactive);
    }
}
