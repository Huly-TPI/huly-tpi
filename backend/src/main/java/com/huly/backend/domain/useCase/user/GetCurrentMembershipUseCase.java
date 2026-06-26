package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.dto.user.GetCurrentMembershipRequest;
import com.huly.backend.domain.dto.user.GetCurrentMembershipResponse;
import com.huly.backend.domain.mapper.user.GetCurrentMembershipMapper;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class GetCurrentMembershipUseCase {

    private final UserPlanRepository userPlanRepository;
    private final GetCurrentMembershipMapper mapper;

    /** Membresía vigente del usuario (inactiva si no tiene o está vencida). */
    public GetCurrentMembershipResponse execute(GetCurrentMembershipRequest request) {
        Instant now = Instant.now();
        return mapper.toResponse(userPlanRepository.findByUser(request.userId())
                .filter(p -> p.isActive(now)));
    }
}
