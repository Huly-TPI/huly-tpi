package com.huly.backend.domain.useCase.badge;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.Badge;
import com.huly.backend.domain.model.UserBadge;
import com.huly.backend.domain.repository.BadgeRepository;
import com.huly.backend.domain.repository.UserBadgeRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@RequiredArgsConstructor
public class GrantBadgeUseCase {
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void execute(String email, String badgeCode) {
        AppUser user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Usuario no encontrado" + email));
        Badge badge = badgeRepository.findByCode(badgeCode).orElseThrow(() -> new NotFoundException("Insignia no encontrada: " + badgeCode));
        if (userBadgeRepository.existsByUserIdAndBadgeCode(user.getId(), badgeCode)) {
            return; 
        }
        userBadgeRepository.save(
    UserBadge.builder()
        .userId(user.getId())
        .badge(badge)
        .obtainedAt(Instant.now())
        .build()
        );
    }
}