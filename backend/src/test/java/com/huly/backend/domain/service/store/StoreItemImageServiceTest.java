package com.huly.backend.domain.service.store;

import com.huly.backend.domain.port.FileStoragePort;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private StoreItemImageService imageService;

    @BeforeEach
    void setUp() {
        when(fileStoragePort.upload(any(), anyString(), any())).thenReturn("http://x/light");
    }

    private List<String> uploadedKeys(String contentType) {
        imageService.uploadThemePair(new byte[] { 1 }, new byte[] { 2 }, contentType);
        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        verify(fileStoragePort, times(2)).upload(any(), keys.capture(), any());
        return keys.getAllValues();
    }

    @Test
    void uploadThemePair_shouldUploadBothThemesWithSameFilenameAndReturnLightUrl() {
        String result = imageService.uploadThemePair(new byte[] { 1 }, new byte[] { 2 }, "image/webp");

        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        verify(fileStoragePort, times(2)).upload(any(), keys.capture(), any());
        String light = keys.getAllValues().get(0);
        String dark = keys.getAllValues().get(1);
        assertThat(light).startsWith("light-theme/").endsWith(".webp");
        assertThat(dark).startsWith("dark-theme/").endsWith(".webp");
        assertThat(light.substring("light-theme/".length()))
                .isEqualTo(dark.substring("dark-theme/".length()));
        assertThat(result).isEqualTo("http://x/light");
    }

    @Test
    void uploadThemePair_shouldMapPngContentTypeToExtension() {
        assertThat(uploadedKeys("image/png").get(0)).endsWith(".png");
    }

    @Test
    void uploadThemePair_shouldMapJpegContentTypeToExtension() {
        assertThat(uploadedKeys("image/jpeg").get(0)).endsWith(".jpg");
    }

    @Test
    void uploadThemePair_shouldMapWebpContentTypeToExtension() {
        assertThat(uploadedKeys("image/webp").get(0)).endsWith(".webp");
    }

    @Test
    void uploadThemePair_shouldUseNoExtension_whenContentTypeUnknown() {
        assertThat(uploadedKeys("image/gif").get(0)).doesNotContain(".");
    }

    @Test
    void uploadThemePair_shouldUseNoExtension_whenContentTypeNull() {
        assertThat(uploadedKeys(null).get(0)).doesNotContain(".");
    }

}
