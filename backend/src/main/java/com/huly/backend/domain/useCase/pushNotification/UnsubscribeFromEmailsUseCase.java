package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.dto.pushNotification.UnsubscribeFromEmailsRequest;
import com.huly.backend.domain.dto.pushNotification.UnsubscribeFromEmailsResponse;
import com.huly.backend.domain.mapper.pushNotification.UnsubscribeFromEmailsMapper;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class UnsubscribeFromEmailsUseCase {

    private final UserRepository userRepository;
    private final UnsubscribeFromEmailsMapper mapper;

    public UnsubscribeFromEmailsResponse execute(UnsubscribeFromEmailsRequest request) {
        Optional<AppUser> user = userRepository.findByUnsubscribeToken(request.token());
        if (user.isEmpty()) {
            return mapper.toResponse(false);
        }
        userRepository.disableReengagementEmails(user.get().getId());
        return mapper.toResponse(true);
    }
}
