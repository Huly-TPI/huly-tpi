package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.UserDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDetailDomainRepositoryImpl implements UserDetailDomainRepository {

    private final UserDetailRepository userDetailRepository;

    @Override
    public Optional<Boolean> findProfileOnBoardingCompleted(Long userId) {
        return userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(userId)
                .map(UserDetailEntity::getProfileOnBoardingCompleted);
    }

    @Override
    @Transactional
    public void completeOnboarding(Long userId, String answer1, String answer2, String answer3) {
        UserDetailEntity userDetail = userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("UserDetail not found for userId: " + userId));
        userDetail.setOnboardingAnswer1(answer1);
        userDetail.setOnboardingAnswer2(answer2);
        userDetail.setOnboardingAnswer3(answer3);
        userDetail.setProfileOnBoardingCompleted(true);
        userDetailRepository.save(userDetail);
    }
    
}
