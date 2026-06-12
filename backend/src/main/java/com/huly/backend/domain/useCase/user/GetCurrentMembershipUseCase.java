package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.dto.payment.UserPlan;
import com.huly.backend.domain.repository.UserPlanRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class GetCurrentMembershipUseCase {

    private final UserPlanRepository userPlanRepository;

    /** Membresía vigente del usuario (vacío si no tiene o está vencida). */
    public Optional<UserPlan> execute(Long userId) {
        Instant now = Instant.now();
        return userPlanRepository.findByUser(userId)
                .filter(p -> p.isActive(now));
    }
}
