package com.huly.backend.domain.useCase.lantern;

import com.huly.backend.domain.model.LanternThought;
import com.huly.backend.domain.repository.LanternThoughtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateLanternThoughtUseCase {

    private final LanternThoughtRepository lanternThoughtRepository;

    public LanternThought execute(Long userId, String text) {
        return lanternThoughtRepository.save(userId, text);
    }
}
