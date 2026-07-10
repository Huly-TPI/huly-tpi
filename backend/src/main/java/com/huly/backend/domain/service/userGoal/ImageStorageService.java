package com.huly.backend.domain.service.userGoal;

import com.huly.backend.domain.port.FileStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private final FileStoragePort fileStoragePort;

    public String save(byte[] content, String contentType) {
        String filename = UUID.randomUUID() + extensionFor(contentType);
        return fileStoragePort.upload(content, "goals/" + filename, contentType);
    }

    private String extensionFor(String contentType) {
        if (contentType == null) {
            return "";
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}
