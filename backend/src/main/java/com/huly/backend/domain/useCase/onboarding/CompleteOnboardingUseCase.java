package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.badge.GrantBadgeUseCase;


@Service
@RequiredArgsConstructor
public class CompleteOnboardingUseCase {
    
    private final UserRepository userRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final UserVectorMemoryService userVectorMemoryService;
    private final GrantBadgeUseCase grantBadgeUseCase;
    @Transactional
    public void execute(String email, String answer1, String answer2, String answer3) { 
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + email));

        userDetailDomainRepository.completeOnboarding(user.getId(), answer1, answer2, answer3);
        try { 
        userVectorMemoryService.rememberOnboardingGoals(user.getId(), answer1, answer2, answer3);
    } catch (Exception e) {
            
        }

        grantBadgeUseCase.execute(email, "PRIMER_PASO");
    }

}
