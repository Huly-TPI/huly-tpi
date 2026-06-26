package com.huly.backend.domain.useCase.lead;

import com.huly.backend.domain.dto.lead.RegisterLeadRequest;
import com.huly.backend.domain.dto.lead.RegisterLeadResponse;
import com.huly.backend.domain.exception.DuplicateResourceException;
import com.huly.backend.domain.mapper.lead.RegisterLeadMapper;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RegisterLeadUseCase {

    private final UserRepository userRepository;
    private final EmailPort emailPort;
    private final RegisterLeadMapper mapper;

    @Transactional
    public RegisterLeadResponse execute(RegisterLeadRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("lead", "email", request.email());
        }
        AppUser saved = userRepository.save(mapper.toModel(request));
        userRepository.saveLeadDetail(saved.getId(), request.nickname(), request.sourceAction());
        emailPort.sendWelcomeLead(request.email(), request.nickname());
        return mapper.toResponse(saved, request);
    }
}
