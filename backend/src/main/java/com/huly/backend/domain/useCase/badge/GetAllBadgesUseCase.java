package com.huly.backend.domain.useCase.badge;
import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.repository.badge.BadgeRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetAllBadgesUseCase {
    private final BadgeRepository badgeRepository;

    public List<Badge> execute() {
        return badgeRepository.findAll();
    }
    
}
