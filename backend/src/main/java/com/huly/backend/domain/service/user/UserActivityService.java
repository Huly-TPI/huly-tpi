package com.huly.backend.domain.service.user;

import com.huly.backend.domain.model.comebackReward.ComebackRewardPolicy;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Registro de la actividad del usuario (última fecha vista) respetando la política de comeback.
 */
@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final UserDetailDomainRepository userDetailDomainRepository;

    /**
     * Registra la actividad del usuario en la fecha dada: avanza last_login_date salvo que exista
     * un comeback pendiente (para no borrar la brecha de inactividad antes de que pueda reclamarlo).
     */
    public void registerActivity(Long userId, LocalDate today) {
        LocalDate lastSeen = userDetailDomainRepository.findLastLoginDate(userId).orElse(null);
        if (ComebackRewardPolicy.shouldRegisterActivity(lastSeen, today)) {
            userDetailDomainRepository.updateLastLoginDate(userId, today);
        }
    }
}
