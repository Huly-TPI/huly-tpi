package com.huly.backend.domain.service.store;

import com.huly.backend.domain.port.FileStoragePort;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class StoreItemImageService {

    private final FileStoragePort fileStoragePort;

    public String uploadThemePair(byte[] light, byte[] dark, String contentType) {
        String filename = UUID.randomUUID() + extensionFor(contentType);
        String lightUrl = fileStoragePort.upload(light, "light-theme/" + filename, contentType);
        fileStoragePort.upload(dark, "dark-theme/" + filename, contentType);
        return lightUrl;
    }

    private String extensionFor(String contentType) {
        if (contentType == null)
            return "";
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}
