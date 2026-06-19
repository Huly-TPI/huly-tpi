package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.badge.GrantBadgeUseCase;

@RequiredArgsConstructor
public class CompleteOnboardingUseCase {
    
    private final UserRepository userRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final UserVectorMemoryService userVectorMemoryService;
    private final GrantBadgeUseCase grantBadgeUseCase;
    @Transactional
    public void execute(Long userId, String answer1, String answer2, String answer3) {
        AppUser user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));

        userDetailDomainRepository.completeOnboarding(user.getId(), answer1, answer2, answer3);
        try { 
            String content = String.format("Goal 1: %s\nGoal 2: %s\nGoal 3: %s", answer1, answer2, answer3);
            userVectorMemoryService.saveMemory(new SaveVectorMemoryCommand(
                    user.getId(),
                    VectorMemorySource.ONBOARDING,
                    user.getId() != null ? user.getId().toString() : null,
                    "ONBOARDING_GOALS",
                    "ONBOARDING_GOALS",
                    content,
                    null,
                    null,
                    java.util.Map.of("createdFrom", "USER_MESSAGE", "feature", "ONBOARDING")
            ));
        } catch (Exception e) {
            
        }

        grantBadgeUseCase.execute(user.getEmail(), "PRIMER_PASO");
    }

}
