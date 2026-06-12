package com.huly.backend.infrastructure.adapter.ai;

import com.huly.backend.domain.exception.ImageValidationUnavailableException;
import com.huly.backend.domain.model.ImageValidationResult;
import com.huly.backend.domain.provider.ImageValidationPort;
import org.springframework.stereotype.Component;

@Component
public class NoOpImageValidationAdapter implements ImageValidationPort {

    @Override
    public ImageValidationResult validate(byte[] imageBytes, String mimeType, String challengeTitle, String challengeDescription) {
        throw new ImageValidationUnavailableException(
                "El servicio de validación de imágenes no está disponible en este entorno",
                new UnsupportedOperationException("No hay un proveedor de IA configurado")
        );
    }
}
