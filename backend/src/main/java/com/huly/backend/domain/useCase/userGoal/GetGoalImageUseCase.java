package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.GetGoalImageRequest;
import com.huly.backend.domain.service.userGoal.ImageStorageService;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor
public class GetGoalImageUseCase {

    private final ImageStorageService imageStorageService;

    /**
     * Devuelve la ruta de la imagen. Se devuelve {@link Path} (tipo de infraestructura/binario)
     * de forma deliberada como excepcion documentada al patron de DTO de dominio:
     * el controller construye el {@code Resource}/{@code MediaType}.
     */
    public Path execute(GetGoalImageRequest request) {
        return imageStorageService.resolve(request.filename());
    }
}
