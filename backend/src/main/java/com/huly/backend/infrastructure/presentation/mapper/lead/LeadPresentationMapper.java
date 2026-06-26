package com.huly.backend.infrastructure.presentation.mapper.lead;

import com.huly.backend.domain.dto.lead.RegisterLeadRequest;
import com.huly.backend.domain.dto.lead.RegisterLeadResponse;
import com.huly.backend.infrastructure.presentation.dto.lead.LeadRequestDto;
import com.huly.backend.infrastructure.presentation.dto.lead.LeadResponseDto;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de leads:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class LeadPresentationMapper {

    public RegisterLeadRequest toRegisterRequest(LeadRequestDto request) {
        return new RegisterLeadRequest(request.email(), request.nickname(), request.sourceAction());
    }

    public LeadResponseDto toLeadResponse(RegisterLeadResponse response) {
        return new LeadResponseDto("Registro exitoso");
    }
}
