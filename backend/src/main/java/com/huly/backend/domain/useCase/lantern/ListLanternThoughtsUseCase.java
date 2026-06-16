package com.huly.backend.domain.useCase.lantern;

import com.huly.backend.domain.model.LanternThought;
import com.huly.backend.domain.repository.LanternThoughtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListLanternThoughtsUseCase {

    private final LanternThoughtRepository lanternThoughtRepository;

    public List<LanternThought> execute(Long userId) {
        return lanternThoughtRepository.findAllByUserId(userId);
    }
}
