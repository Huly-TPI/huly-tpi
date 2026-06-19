package com.huly.backend.infrastructure.repository.jpaRepository.implementation;
import com.huly.backend.domain.model.UserBadge;
import com.huly.backend.domain.repository.user.UserBadgeRepository;
import com.huly.backend.infrastructure.repository.entity.BadgeEntity;
import com.huly.backend.infrastructure.repository.entity.UserBadgeEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserBadgeJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.UserBadgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IBadgeJpaRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserBadgeRepositoryImpl implements UserBadgeRepository {
    private final IUserBadgeJpaRepository userBadgeJpaRepository;
    private final UserBadgeMapper userBadgeMapper;
    private final IBadgeJpaRepository iBadgeJpaRepository;

    @Override
    public List<UserBadge> findAllByUserId(Long userId) {
        return userBadgeJpaRepository.findAllByUserId(userId).stream().map(userBadgeMapper::toDomain).toList();
    }

    @Override
    public boolean existsByUserIdAndBadgeCode(Long userId, String badgeCode) {
        return userBadgeJpaRepository.existsByUserIdAndBadgeCode(userId, badgeCode);
    }

    @Override
    public UserBadge save(UserBadge userBadge) {
        BadgeEntity badgeRef = iBadgeJpaRepository.getReferenceById(userBadge.getBadge().getId());
        UserBadgeEntity entity = UserBadgeEntity.builder()
                .userId(userBadge.getUserId())
                .badge(badgeRef)
                .obtainedAt(userBadge.getObtainedAt())
                .build();
        return userBadgeMapper.toDomain(userBadgeJpaRepository.save(entity));
    }
    
}
