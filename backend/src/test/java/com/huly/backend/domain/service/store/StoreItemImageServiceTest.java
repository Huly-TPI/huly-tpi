package com.huly.backend.domain.service.store;

import com.huly.backend.domain.port.FileStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreItemImageServiceTest {

    private static final byte[] LIGHT = {1};
    private static final byte[] DARK = {2};
    private static final String LIGHT_URL = "http://x/light";
    private static final String LIGHT_PREFIX = "light-theme/";
    private static final String DARK_PREFIX = "dark-theme/";

    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private StoreItemImageService imageService;

    @BeforeEach
    void setUp() {
        when(fileStoragePort.upload(any(), anyString(), any())).thenReturn(LIGHT_URL);
    }

    @Test
    @DisplayName("Sube ambos temas con el mismo nombre y devuelve la URL del tema claro")
    void uploadThemePairShouldUploadBothThemesWithSameFilenameAndReturnLightUrl() {
        String result = uploadThemePair("image/webp");

        thenBothThemesShareFilenameWithExtension(".webp");
        thenReturnedUrlIs(result, LIGHT_URL);
    }

    @Test
    @DisplayName("Mapea el content type png a su extensión")
    void uploadThemePairShouldMapPngContentTypeToExtension() {
        uploadThemePair("image/png");

        thenLightKeyEndsWith(".png");
    }

    @Test
    @DisplayName("Mapea el content type jpeg a su extensión")
    void uploadThemePairShouldMapJpegContentTypeToExtension() {
        uploadThemePair("image/jpeg");

        thenLightKeyEndsWith(".jpg");
    }

    @Test
    @DisplayName("Mapea el content type webp a su extensión")
    void uploadThemePairShouldMapWebpContentTypeToExtension() {
        uploadThemePair("image/webp");

        thenLightKeyEndsWith(".webp");
    }

    @Test
    @DisplayName("No usa extensión cuando el content type es desconocido")
    void uploadThemePairShouldUseNoExtensionWhenContentTypeUnknown() {
        uploadThemePair("image/gif");

        thenLightKeyHasNoExtension();
    }

    @Test
    @DisplayName("No usa extensión cuando el content type es nulo")
    void uploadThemePairShouldUseNoExtensionWhenContentTypeNull() {
        uploadThemePair(null);

        thenLightKeyHasNoExtension();
    }

    // --- act ---
    private String uploadThemePair(String contentType) {
        return imageService.uploadThemePair(LIGHT, DARK, contentType);
    }

    // --- assert ---
    private List<String> capturedKeys() {
        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        verify(fileStoragePort, times(2)).upload(any(), keys.capture(), any());
        return keys.getAllValues();
    }

    private void thenBothThemesShareFilenameWithExtension(String suffix) {
        List<String> keys = capturedKeys();
        String light = keys.get(0);
        String dark = keys.get(1);
        assertThat(light).startsWith(LIGHT_PREFIX).endsWith(suffix);
        assertThat(dark).startsWith(DARK_PREFIX).endsWith(suffix);
        assertThat(light.substring(LIGHT_PREFIX.length()))
                .isEqualTo(dark.substring(DARK_PREFIX.length()));
    }

    private void thenReturnedUrlIs(String result, String expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenLightKeyEndsWith(String suffix) {
        assertThat(capturedKeys().get(0)).endsWith(suffix);
    }

    private void thenLightKeyHasNoExtension() {
        assertThat(capturedKeys().get(0)).doesNotContain(".");
    }
}
