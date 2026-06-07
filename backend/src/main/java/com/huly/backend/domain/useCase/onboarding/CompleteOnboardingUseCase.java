package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;


@Service
@RequiredArgsConstructor
public class CompleteOnboardingUseCase {
    
    private final UserRepository userRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final UserVectorMemoryService userVectorMemoryService;
    @Transactional
    public void execute(Long userId, String answer1, String answer2, String answer3) {
        AppUser user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));

        userDetailDomainRepository.completeOnboarding(user.getId(), answer1, answer2, answer3);
        try { 
        userVectorMemoryService.rememberOnboardingGoals(user.getId(), answer1, answer2, answer3);
    } catch (Exception e) {
            
        }
    }

}
