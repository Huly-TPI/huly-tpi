package com.huly.backend.infrastructure.presentation.controller;
import com.huly.backend.domain.useCase.breathingTechnique.GetBreathingTechniquesUseCase;
import com.huly.backend.infrastructure.presentation.dto.breathingTechniques.BreathingTechniqueResponse;
import com.huly.backend.infrastructure.presentation.mapper.breathing.BreathingPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequestMapping("/api/breathing")
@RequiredArgsConstructor
@PreAuthorize("permitAll()")
public class BreathingController {

     private final GetBreathingTechniquesUseCase getBreathingTechniquesUseCase;
     private final BreathingPresentationMapper breathingPresentationMapper;

     @GetMapping("/techniques")
        public ResponseEntity<List<BreathingTechniqueResponse>> getAllBreathingTechniques() {
            return ResponseEntity.ok(
                    breathingPresentationMapper.toTechniqueResponses(getBreathingTechniquesUseCase.execute()));
        }
}
