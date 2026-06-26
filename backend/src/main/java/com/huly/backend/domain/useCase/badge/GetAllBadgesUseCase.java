package com.huly.backend.domain.useCase.badge;
import com.huly.backend.domain.dto.badge.GetAllBadgesResponse;
import com.huly.backend.domain.mapper.badge.GetAllBadgesMapper;
import com.huly.backend.domain.repository.badge.BadgeRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetAllBadgesUseCase {
    private final BadgeRepository badgeRepository;
    private final GetAllBadgesMapper mapper;

    public GetAllBadgesResponse execute() {
        return mapper.toResponse(badgeRepository.findAll());
    }

}
