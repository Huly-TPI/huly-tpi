package com.huly.backend.infrastructure.adapter.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LocalFileStorageAdapterTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LocalFileStorageAdapter();
        ReflectionTestUtils.setField(adapter, "uploadsDir", tempDir.toString());
        ReflectionTestUtils.setField(adapter, "publicPath", "/api/store/images");
    }

    @Test
    void upload_shouldWriteFileAndReturnPublicUrl() throws IOException {
        String url = adapter.upload(new byte[] { 1, 2, 3 }, "light-theme/abc.webp", "image/webp");
        assertThat(url).isEqualTo("/api/store/images/light-theme/abc.webp");
        Path written = tempDir.resolve("light-theme/abc.webp");
        assertThat(Files.exists(written)).isTrue();
        assertThat(Files.readAllBytes(written)).isEqualTo(new byte[] { 1, 2, 3 });
    }

    @Test
    void upload_shouldStripLeadingSlashFromObjectKey() {
        String url = adapter.upload(new byte[] { 1 }, "/dark-theme/x.png", "image/png");
        assertThat(url).isEqualTo("/api/store/images/dark-theme/x.png");
    }

    @Test
    void upload_shouldStripTrailingSlashFromPublicPath() {
        ReflectionTestUtils.setField(adapter, "publicPath", "/api/store/images/");
        String url = adapter.upload(new byte[] { 1 }, "light-theme/y.webp", "image/webp");
        assertThat(url).isEqualTo("/api/store/images/light-theme/y.webp");
    }

    @Test
    void upload_shouldThrowIllegalState_whenWriteFails() throws IOException {
        Path notADir = tempDir.resolve("not-a-dir");
        Files.writeString(notADir, "x");
        ReflectionTestUtils.setField(adapter, "uploadsDir", notADir.toString());

        assertThatThrownBy(() -> adapter.upload(new byte[] { 1 }, "light-theme/x.webp", "image/webp"))
                .isInstanceOf(IllegalStateException.class);
    }

}
