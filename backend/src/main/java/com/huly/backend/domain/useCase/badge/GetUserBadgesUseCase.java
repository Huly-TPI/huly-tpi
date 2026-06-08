package com.huly.backend.domain.useCase.badge;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.UserBadge;
import com.huly.backend.domain.repository.UserBadgeRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetUserBadgesUseCase {

    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;

    public List<UserBadge> execute(String email) {
        AppUser user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return userBadgeRepository.findAllByUserId(user.getId());
    }
    
}
