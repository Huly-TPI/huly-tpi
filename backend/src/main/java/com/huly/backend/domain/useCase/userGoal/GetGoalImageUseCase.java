package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.service.userGoal.ImageStorageService;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor
public class GetGoalImageUseCase {

    private final ImageStorageService imageStorageService;

    public Path execute(String filename) {
        return imageStorageService.resolve(filename);
    }
}
