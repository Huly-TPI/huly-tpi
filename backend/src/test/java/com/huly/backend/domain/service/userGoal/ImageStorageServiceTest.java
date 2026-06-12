package com.huly.backend.domain.service.userGoal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ImageStorageServiceTest {

    @TempDir
    Path tempDir;

    private ImageStorageService service;

    @BeforeEach
    void setUp() {
        service = new ImageStorageService();
        ReflectionTestUtils.setField(service, "uploadsDir", tempDir.toString());
    }

    @Test
    void save_shouldReturnUrlWithCorrectPrefix() throws Exception {
        MultipartFile file = mockFile("foto.jpg");

        String url = service.save(file);

        assertThat(url).startsWith("/api/user-goals/images/");
    }

    @Test
    void save_shouldPreserveFileExtension() throws Exception {
        MultipartFile file = mockFile("foto.png");

        String url = service.save(file);

        assertThat(url).endsWith(".png");
    }

    @Test
    void save_shouldWriteFileToUploadDir() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("foto.jpg");
        doAnswer(invocation -> {
            Path dest = invocation.getArgument(0);
            Files.write(dest, new byte[]{1, 2, 3});
            return null;
        }).when(file).transferTo(any(Path.class));

        String url = service.save(file);

        String filename = url.substring("/api/user-goals/images/".length());
        assertThat(tempDir.resolve(filename)).exists();
    }

    @Test
    void save_shouldThrowIllegalStateException_whenTransferToFails() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("foto.jpg");
        doThrow(new IOException("disk full")).when(file).transferTo(any(Path.class));

        assertThatThrownBy(() -> service.save(file))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se pudo guardar la imagen del reto");
    }

    @Test
    void save_shouldHandleFilenameWithoutExtension() throws Exception {
        MultipartFile file = mockFile("foto");

        String url = service.save(file);

        assertThat(url).startsWith("/api/user-goals/images/");
        assertThat(url).doesNotContain(".");
    }

    @Test
    void resolve_shouldReturnPathUnderUploadsDir() {
        Path resolved = service.resolve("photo.jpg");

        assertThat(resolved).isEqualTo(tempDir.resolve("photo.jpg"));
    }

    private MultipartFile mockFile(String originalFilename) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        doNothing().when(file).transferTo(any(Path.class));
        return file;
    }
}
