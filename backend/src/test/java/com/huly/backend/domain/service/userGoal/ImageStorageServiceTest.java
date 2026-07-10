package com.huly.backend.domain.service.userGoal;

import com.huly.backend.domain.port.FileStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageStorageServiceTest {

    private static final byte[] CONTENT = {1, 2, 3};
    private static final String GOALS_PREFIX = "goals/";
    private static final String RETURNED_URL = "https://bucket.example/goals/abc.jpg";

    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private ImageStorageService service;

    @BeforeEach
    void setUp() {
        when(fileStoragePort.upload(any(), anyString(), any())).thenReturn(RETURNED_URL);
    }

    @Test
    @DisplayName("Sube el contenido bajo la carpeta goals/ y devuelve la URL del bucket")
    void saveShouldUploadUnderGoalsPrefixAndReturnBucketUrl() {
        String result = save("image/jpeg");

        thenReturnedUrlIs(result);
        thenUploadedKeyStartsWith(GOALS_PREFIX);
    }

    @Test
    @DisplayName("Mapea el content type png a su extensión")
    void saveShouldMapPngContentTypeToExtension() {
        save("image/png");

        thenUploadedKeyEndsWith(".png");
    }

    @Test
    @DisplayName("Mapea el content type jpeg a su extensión")
    void saveShouldMapJpegContentTypeToExtension() {
        save("image/jpeg");

        thenUploadedKeyEndsWith(".jpg");
    }

    @Test
    @DisplayName("Mapea el content type gif a su extensión")
    void saveShouldMapGifContentTypeToExtension() {
        save("image/gif");

        thenUploadedKeyEndsWith(".gif");
    }

    @Test
    @DisplayName("Mapea el content type webp a su extensión")
    void saveShouldMapWebpContentTypeToExtension() {
        save("image/webp");

        thenUploadedKeyEndsWith(".webp");
    }

    @Test
    @DisplayName("No usa extensión cuando el content type es desconocido")
    void saveShouldUseNoExtensionWhenContentTypeUnknown() {
        save("application/octet-stream");

        thenUploadedKeyHasNoExtension();
    }

    @Test
    @DisplayName("No usa extensión cuando el content type es nulo")
    void saveShouldUseNoExtensionWhenContentTypeNull() {
        save(null);

        thenUploadedKeyHasNoExtension();
    }

    // --- act ---
    private String save(String contentType) {
        return service.save(CONTENT, contentType);
    }

    // --- assert ---
    private String capturedKey() {
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(fileStoragePort).upload(any(), key.capture(), any());
        return key.getValue();
    }

    private void thenReturnedUrlIs(String result) {
        assertThat(result).isEqualTo(RETURNED_URL);
    }

    private void thenUploadedKeyStartsWith(String prefix) {
        assertThat(capturedKey()).startsWith(prefix);
    }

    private void thenUploadedKeyEndsWith(String suffix) {
        assertThat(capturedKey()).endsWith(suffix);
    }

    private void thenUploadedKeyHasNoExtension() {
        String key = capturedKey();
        assertThat(key.substring(GOALS_PREFIX.length())).doesNotContain(".");
    }
}
