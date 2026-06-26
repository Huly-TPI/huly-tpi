package com.huly.backend.domain.useCase.badge;

import com.huly.backend.domain.dto.badge.GrantBadgeRequest;
import com.huly.backend.domain.dto.badge.GrantBadgeResponse;
import com.huly.backend.domain.mapper.badge.GrantBadgeMapper;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.repository.badge.BadgeRepository;
import com.huly.backend.domain.repository.user.UserBadgeRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class GrantBadgeUseCase {
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final GrantBadgeMapper mapper;

    @Transactional
    public GrantBadgeResponse execute(GrantBadgeRequest request) {
        AppUser user = userRepository.findByEmail(request.email()).orElseThrow(() -> new NotFoundException("Usuario no encontrado" + request.email()));
        Badge badge = badgeRepository.findByCode(request.badgeCode()).orElseThrow(() -> new NotFoundException("Insignia no encontrada: " + request.badgeCode()));
        if (userBadgeRepository.existsByUserIdAndBadgeCode(user.getId(), request.badgeCode())) {
            return mapper.toResponse(false);
        }
        userBadgeRepository.save(mapper.toModel(user.getId(), badge));
        return mapper.toResponse(true);
    }
}
