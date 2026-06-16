package com.huly.backend.domain.useCase.lantern;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.repository.LanternThoughtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarkWorkedOnUseCase {

    private final LanternThoughtRepository lanternThoughtRepository;

    public void execute(Long id, Long userId) {
        lanternThoughtRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("LanternThought", "id", id));
        lanternThoughtRepository.markWorkedOn(id);
    }
}
