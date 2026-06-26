package com.huly.backend.domain.mapper.lead;

import com.huly.backend.domain.dto.lead.RegisterLeadRequest;
import com.huly.backend.domain.dto.lead.RegisterLeadResponse;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;

/**
 * Mapper de dominio para el caso de uso de registro de lead.
 */
public class RegisterLeadMapper {

    public AppUser toModel(RegisterLeadRequest request) {
        return AppUser.builder()
                .email(request.email())
                .role(UserRole.LEAD)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public RegisterLeadResponse toResponse(AppUser saved, RegisterLeadRequest request) {
        return new RegisterLeadResponse(
                saved.getId(),
                saved.getEmail(),
                request.nickname()
        );
    }
}
