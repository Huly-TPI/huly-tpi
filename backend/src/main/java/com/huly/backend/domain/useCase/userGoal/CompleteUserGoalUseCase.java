package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.exception.ImageValidationUnavailableException;
import com.huly.backend.domain.exception.InvalidGoalImageException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.goals.ImageValidationResult;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.port.ImageValidationPort;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import com.huly.backend.domain.repository.UserPlantRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.service.userGoal.ImageStorageService;
import com.huly.backend.domain.useCase.userPlant.GetOrCreateCurrentPlantUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

@RequiredArgsConstructor
public class CompleteUserGoalUseCase {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

    private final UserGoalRepository userGoalRepository;
    private final UserPlantRepository userPlantRepository;
    private final GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase;
    private final CoinService coinService;
    private final ImageStorageService imageStorageService;
    private final ImageValidationPort imageValidationPort;

    public record Result(UserGoal goal, boolean harvestTriggered, Integer harvestedPlantNumber, UserPlant currentPlant) {}

    @Transactional
    public Result execute(Long id, MultipartFile image) {
        UserGoal goal = userGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGoal", "id", id));

        if (goal.getStatus() == GoalStatus.COMPLETED) {
            UserPlant current = getOrCreateCurrentPlantUseCase.execute(goal.getUserId());
            long count = userPlantRepository.countCompletedGoalsByPlantId(current.getId());
            return new Result(goal, false, null, withCount(current, count));
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            validateFileSize(image);
            validateMimeType(image);
            imageUrl = validateAndSave(image, goal);
        }

        UserPlant currentPlant = getOrCreateCurrentPlantUseCase.execute(goal.getUserId());

        goal.setStatus(GoalStatus.COMPLETED);
        goal.setImageUrl(imageUrl);
        goal.setUserPlantId(currentPlant.getId());
        UserGoal savedGoal = userGoalRepository.save(goal);

        int coins = imageUrl != null
                ? (goal.getCoinsRewardWithImage() != null ? goal.getCoinsRewardWithImage() : 25)
                : (goal.getCoinsReward() != null ? goal.getCoinsReward() : 10);
        coinService.credit(goal.getUserId(), coins);

        long completedCount = userPlantRepository.countCompletedGoalsByPlantId(currentPlant.getId());

        if (completedCount >= currentPlant.getRequiredGoals()) {
            currentPlant.setStatus(PlantStatus.COMPLETED);
            currentPlant.setCompletedAt(Instant.now());
            userPlantRepository.saveAndFlush(currentPlant);

            int nextNumber = currentPlant.getPlantNumber() + 1;
            UserPlant nextPlant = userPlantRepository.save(UserPlant.builder()
                    .userId(goal.getUserId())
                    .plantNumber(nextNumber)
                    .requiredGoals(GetOrCreateCurrentPlantUseCase.calculateRequiredGoals(nextNumber))
                    .status(PlantStatus.GROWING)
                    .startedAt(Instant.now())
                    .build());

            return new Result(savedGoal, true, currentPlant.getPlantNumber(), withCount(nextPlant, 0));
        }

        return new Result(savedGoal, false, null, withCount(currentPlant, completedCount));
    }

    private UserPlant withCount(UserPlant plant, long count) {
        plant.setCompletedGoalsCount(count);
        return plant;
    }

    private void validateFileSize(MultipartFile image) {
        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new InvalidGoalImageException("La imagen no puede superar los 5 MB.");
        }
    }

    private void validateMimeType(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidGoalImageException(
                    "Formato de imagen no soportado. Solo se aceptan JPEG, PNG, GIF y WebP."
            );
        }
    }

    private String validateAndSave(MultipartFile image, UserGoal goal) {
        byte[] bytes;
        try {
            bytes = image.getBytes();
        } catch (IOException e) {
            throw new ImageValidationUnavailableException("No se pudo leer la imagen", e);
        }

        ImageValidationResult result = imageValidationPort.validate(
                bytes, image.getContentType(), goal.getTitle(), goal.getDescription()
        );
        if (!result.valid()) {
            throw new InvalidGoalImageException(result.reason());
        }

        return imageStorageService.save(image);
    }
}
