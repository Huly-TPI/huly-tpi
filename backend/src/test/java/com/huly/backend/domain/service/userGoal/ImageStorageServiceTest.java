package com.huly.backend.domain.service.userGoal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImageStorageServiceTest {

    private static final String IMAGES_PREFIX = "/api/user-goals/images/";

    @TempDir
    Path tempDir;

    private ImageStorageService service;

    @BeforeEach
    void setUp() {
        service = new ImageStorageService();
        ReflectionTestUtils.setField(service, "uploadsDir", tempDir.toString());
    }

    @Test
    @DisplayName("Devuelve una URL con el prefijo correcto")
    void saveShouldReturnUrlWithCorrectPrefix() throws Exception {
        MultipartFile file = fileNamed("foto.jpg");

        String url = save(file);

        thenUrlHasImagesPrefix(url);
    }

    @Test
    @DisplayName("Conserva la extensión del archivo")
    void saveShouldPreserveFileExtension() throws Exception {
        MultipartFile file = fileNamed("foto.png");

        String url = save(file);

        thenUrlEndsWith(url, ".png");
    }

    @Test
    @DisplayName("Escribe el archivo en el directorio de subidas")
    void saveShouldWriteFileToUploadDir() throws Exception {
        MultipartFile file = fileWritingBytes("foto.jpg");

        String url = save(file);

        thenFileExistsInUploadDir(url);
    }

    @Test
    @DisplayName("Lanza IllegalStateException cuando falla la transferencia")
    void saveShouldThrowIllegalStateExceptionWhenTransferToFails() throws Exception {
        MultipartFile file = fileFailingTransfer("foto.jpg");

        thenSaveThrowsIllegalState(file);
    }

    @Test
    @DisplayName("Maneja nombres de archivo sin extensión")
    void saveShouldHandleFilenameWithoutExtension() throws Exception {
        MultipartFile file = fileNamed("foto");

        String url = save(file);

        thenUrlHasNoExtension(url);
    }

    @Test
    @DisplayName("Maneja el nombre de archivo nulo")
    void saveShouldHandleNullFilename() throws Exception {
        MultipartFile file = fileNamed(null);

        String url = save(file);

        thenUrlHasNoExtension(url);
    }

    @Test
    @DisplayName("Resuelve la ruta bajo el directorio de subidas")
    void resolveShouldReturnPathUnderUploadsDir() {
        Path resolved = resolve("photo.jpg");

        thenResolvedPathIsUnderUploadsDir(resolved, "photo.jpg");
    }

    // --- arrange ---
    private MultipartFile fileNamed(String originalFilename) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        doNothing().when(file).transferTo(any(Path.class));
        return file;
    }

    private MultipartFile fileWritingBytes(String originalFilename) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        doAnswer(invocation -> {
            Path dest = invocation.getArgument(0);
            Files.write(dest, new byte[]{1, 2, 3});
            return null;
        }).when(file).transferTo(any(Path.class));
        return file;
    }

    private MultipartFile fileFailingTransfer(String originalFilename) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        doThrow(new IOException("disk full")).when(file).transferTo(any(Path.class));
        return file;
    }

    // --- act ---
    private String save(MultipartFile file) {
        return service.save(file);
    }

    private Path resolve(String filename) {
        return service.resolve(filename);
    }

    // --- assert ---
    private void thenUrlHasImagesPrefix(String url) {
        assertThat(url).startsWith(IMAGES_PREFIX);
    }

    private void thenUrlEndsWith(String url, String suffix) {
        assertThat(url).endsWith(suffix);
    }

    private void thenUrlHasNoExtension(String url) {
        assertThat(url).startsWith(IMAGES_PREFIX);
        assertThat(url).doesNotContain(".");
    }

    private void thenFileExistsInUploadDir(String url) {
        String filename = url.substring(IMAGES_PREFIX.length());
        assertThat(tempDir.resolve(filename)).exists();
    }

    private void thenSaveThrowsIllegalState(MultipartFile file) {
        assertThatThrownBy(() -> service.save(file))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se pudo guardar la imagen del reto");
    }

    private void thenResolvedPathIsUnderUploadsDir(Path resolved, String filename) {
        assertThat(resolved).isEqualTo(tempDir.resolve(filename));
    }
}
