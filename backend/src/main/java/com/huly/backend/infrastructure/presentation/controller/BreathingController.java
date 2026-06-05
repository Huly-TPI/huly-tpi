package com.huly.backend.infrastructure.presentation.controller;
import com.huly.backend.domain.model.BreathingTechnique;
import com.huly.backend.domain.useCase.BreathingSession.GetBreathingTechniquesUseCase;
import com.huly.backend.infrastructure.presentation.dto.BreathingTechniqueResponse;
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

     @GetMapping("/techniques")
        public ResponseEntity<List<BreathingTechniqueResponse>> getAllBreathingTechniques() {
            List<BreathingTechnique> techniques = getBreathingTechniquesUseCase.execute();
            List<BreathingTechniqueResponse> response = techniques.stream()
                    .map(this::toResponse)
                    .toList();
            return ResponseEntity.ok(response);
        }

    private BreathingTechniqueResponse toResponse(BreathingTechnique technique) {
      return new BreathingTechniqueResponse(
              technique.getId(),
              technique.getName(),
              technique.getDescription(),
              technique.getInhaleSeconds(),
              technique.getHoldSeconds(),
              technique.getExhaleSeconds(),
              technique.getRoundsInterval(),
              technique.getRounds()
      );
}
}