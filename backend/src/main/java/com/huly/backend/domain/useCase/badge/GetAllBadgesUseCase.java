package com.huly.backend.domain.useCase.badge;
import com.huly.backend.domain.model.Badge;
import com.huly.backend.domain.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetAllBadgesUseCase {
    private final BadgeRepository badgeRepository;

    public List<Badge> execute() {
        return badgeRepository.findAll();
    }
    
}
