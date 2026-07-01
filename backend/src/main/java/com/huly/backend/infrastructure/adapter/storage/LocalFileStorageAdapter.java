package com.huly.backend.infrastructure.adapter.storage;

import com.huly.backend.domain.port.FileStoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.supabase.storage.enabled", havingValue = "false", matchIfMissing = true)
public class LocalFileStorageAdapter implements FileStoragePort {

     @Value("${app.uploads.store-dir:uploads/store}")
    private String uploadsDir;

    @Value("${app.uploads.store-public-path:/api/store/images}")
    private String publicPath;

    @Override
    public String upload(byte[] content, String objectKey, String contentType) {
         String key = stripLeadingSlash(objectKey);
        Path target = Paths.get(uploadsDir).resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la imagen del producto: " + key, e);
        }
        String url = stripTrailingSlash(publicPath) + "/" + key;
        log.debug("Imagen de producto guardada localmente: {} -> {}", target, url);
        return url;
    }

    private String stripTrailingSlash(String path) {
        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private String stripLeadingSlash(String key) {
        return (key != null && key.startsWith("/")) ? key.substring(1) : key;
    }

    
}
