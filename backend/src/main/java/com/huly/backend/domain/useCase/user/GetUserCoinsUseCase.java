package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.dto.user.GetUserCoinsRequest;
import com.huly.backend.domain.dto.user.GetUserCoinsResponse;
import com.huly.backend.domain.mapper.user.GetUserCoinsMapper;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetUserCoinsUseCase {

    private final UserRepository userRepository;
    private final GetUserCoinsMapper mapper;

    public GetUserCoinsResponse execute(GetUserCoinsRequest request) {
        return mapper.toResponse(userRepository.getCoins(request.userId()));
    }
}
