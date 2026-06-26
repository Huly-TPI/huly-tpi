package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.dto.onboarding.CompleteOnboardingRequest;
import com.huly.backend.domain.dto.onboarding.CompleteOnboardingResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.onboarding.CompleteOnboardingMapper;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.dto.badge.GrantBadgeRequest;
import com.huly.backend.domain.useCase.badge.GrantBadgeUseCase;

@RequiredArgsConstructor
public class CompleteOnboardingUseCase {

    private final UserRepository userRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final UserVectorMemoryService userVectorMemoryService;
    private final GrantBadgeUseCase grantBadgeUseCase;
    private final CompleteOnboardingMapper mapper;
    @Transactional
    public CompleteOnboardingResponse execute(CompleteOnboardingRequest request) {
        Long userId = request.userId();
        String answer1 = request.answer1();
        String answer2 = request.answer2();
        String answer3 = request.answer3();
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

        grantBadgeUseCase.execute(new GrantBadgeRequest(user.getEmail(), "PRIMER_PASO"));
        return mapper.toResponse();
    }

}
