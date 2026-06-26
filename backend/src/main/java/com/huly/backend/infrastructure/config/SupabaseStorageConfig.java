package com.huly.backend.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Configura el cliente S3 (AWS SDK v2) apuntando al endpoint S3-compatible de Supabase Storage.
 * Solo se activa cuando {@code app.supabase.storage.enabled=true}.
 */
@Configuration
@ConditionalOnProperty(name = "app.supabase.storage.enabled", havingValue = "true")
public class SupabaseStorageConfig {

    @Value("${app.supabase.storage.endpoint}")
    private String endpoint;

    @Value("${app.supabase.storage.region:us-east-1}")
    private String region;

    @Value("${app.supabase.storage.access-key}")
    private String accessKey;

    @Value("${app.supabase.storage.secret-key}")
    private String secretKey;

    @Bean
    public S3Client supabaseS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true) // Supabase exige path-style access
                .build();
    }
}
