package com.huly.backend.domain.useCase.badge;
import com.huly.backend.domain.model.Badge;
import com.huly.backend.domain.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllBadgesUseCase {
    private final BadgeRepository badgeRepository;

    public List<Badge> execute() {
        return badgeRepository.findAll();
    }
    
}
