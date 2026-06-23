package com.huly.backend.domain.useCase.userPlant;

import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.domain.repository.UserPlantRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetUserPlantsUseCase {

    private final UserPlantRepository userPlantRepository;

    public List<UserPlant> execute(Long userId) {
        return userPlantRepository.findAllByUserIdOrderByPlantNumber(userId);
    }
}
