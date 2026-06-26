package com.huly.backend.infrastructure.adapter.storage;

import com.huly.backend.domain.port.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Adaptador que sube archivos al bucket S3 de Supabase Storage usando el AWS SDK v2.
 * Devuelve la URL pública permanente del objeto subido.
 * Solo se registra cuando {@code app.supabase.storage.enabled=true}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.supabase.storage.enabled", havingValue = "true")
public class SupabaseS3StorageAdapter implements FileStoragePort {

    private final S3Client s3Client;

    @Value("${app.supabase.storage.bucket}")
    private String bucket;

    @Value("${app.supabase.storage.public-url}")
    private String publicUrl;

    @Override
    public String upload(byte[] content, String objectKey, String contentType) {
        String key = stripLeadingSlash(objectKey);
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content));
        } catch (S3Exception e) {
            throw new IllegalStateException(
                    "No se pudo subir el archivo a Supabase Storage: " + key, e);
        }
        String url = buildPublicUrl(key);
        log.debug("Archivo subido a Supabase Storage: bucket={} key={} url={}", bucket, key, url);
        return url;
    }

    // URL pública: {publicUrl}/{bucket}/{key}
    private String buildPublicUrl(String key) {
        String base = publicUrl;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + bucket + "/" + key;
    }

    private String stripLeadingSlash(String key) {
        return (key != null && key.startsWith("/")) ? key.substring(1) : key;
    }
}
