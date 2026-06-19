package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.exception.ImageValidationUnavailableException;
import com.huly.backend.domain.exception.InvalidGoalImageException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.ImageValidationResult;
import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.port.ImageValidationPort;
import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.service.userGoal.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
public class CompleteUserGoalUseCase {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB

    private final UserGoalRepository userGoalRepository;
    private final CoinService coinService;
    private final ImageStorageService imageStorageService;
    private final ImageValidationPort imageValidationPort;

    @Transactional
    public UserGoal execute(Long id, MultipartFile image) {
        UserGoal goal = userGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGoal", "id", id));
        if (goal.getStatus() == GoalStatus.COMPLETED) {
            return goal;
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            validateFileSize(image);
            validateMimeType(image);
            imageUrl = validateAndSave(image, goal);
        }

        goal.setStatus(GoalStatus.COMPLETED);
        goal.setImageUrl(imageUrl);

        UserGoal saved = userGoalRepository.save(goal);

        int coins = imageUrl != null
                ? (goal.getCoinsRewardWithImage() != null ? goal.getCoinsRewardWithImage() : 25)
                : (goal.getCoinsReward() != null ? goal.getCoinsReward() : 10);
        coinService.credit(goal.getUserId(), coins);

        return saved;
    }

    private void validateFileSize(MultipartFile image) {
        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new InvalidGoalImageException(
                    "La imagen no puede superar los 5 MB."
            );
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
