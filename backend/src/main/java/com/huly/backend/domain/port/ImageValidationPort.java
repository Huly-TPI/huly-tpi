package com.huly.backend.domain.port;

import com.huly.backend.domain.model.ImageValidationResult;

public interface ImageValidationPort {

    ImageValidationResult validate(byte[] imageBytes, String mimeType, String challengeTitle, String challengeDescription);
}
