package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.dto.admin.activities.UpdateActivityConfigRequest;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.activities.UpdateActivityConfigMapper;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateActivityConfigUseCase {

    private final ActivityRepository activityRepository;
    private final UpdateActivityConfigMapper mapper;

    public Activity execute(Long id, UpdateActivityConfigRequest request) {
        Activity existing = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", "id", id));
        validate(request);
        Activity updated = mapper.toModel(existing, request);

        return activityRepository.save(updated);
    }

    private void validate(UpdateActivityConfigRequest request) {
        validateRange("valenceMin", request.valenceMin(), -1.0, 1.0);
        validateRange("valenceMax", request.valenceMax(), -1.0, 1.0);
        validateRange("arousalMin", request.arousalMin(), -1.0, 1.0);
        validateRange("arousalMax", request.arousalMax(), -1.0, 1.0);
        validateRange("dominanceMin", request.dominanceMin(), -1.0, 1.0);
        validateRange("dominanceMax", request.dominanceMax(), -1.0, 1.0);

        if (request.valenceMin() > request.valenceMax())
            throw new BusinessRuleException("valenceMin no puede ser mayor que valenceMax");

        if (request.arousalMin() > request.arousalMax())
            throw new BusinessRuleException("arousalMin no puede ser mayor que arousalMax");

        if (request.dominanceMin() > request.dominanceMax())
            throw new BusinessRuleException("dominanceMin no puede ser mayor que dominanceMax");

        validateRange("effectValence", request.effectValence(), -1.0, 1.0);
        validateRange("effectArousal", request.effectArousal(), -1.0, 1.0);
        validateRange("effectDominance", request.effectDominance(), -1.0, 1.0);

        if (request.title() == null || request.title().isBlank())
            throw new BusinessRuleException("El título es obligatorio");

        if (request.description() == null || request.description().isBlank())
            throw new BusinessRuleException("La descripción es obligatoria");

        if (request.routePath() == null || request.routePath().isBlank())
            throw new BusinessRuleException("La ruta de navegación es obligatoria");
    }

    private void validateRange(String field, double value, double min, double max) {
        if (value < min || value > max) {
            throw new BusinessRuleException(field + " debe estar entre " + min + " y " + max);
        }
    }
}
