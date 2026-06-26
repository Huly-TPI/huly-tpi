package com.huly.backend.infrastructure.adapter.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupabaseS3StorageAdapterTest {

    private static final String PUBLIC_URL = "https://ref.supabase.co/storage/v1/object/public";

    private S3Client s3Client;
    private SupabaseS3StorageAdapter adapter;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        adapter = new SupabaseS3StorageAdapter(s3Client);
        ReflectionTestUtils.setField(adapter, "bucket", "media");
        ReflectionTestUtils.setField(adapter, "publicUrl", PUBLIC_URL);
    }

    @Test
    void upload_shouldPutObjectWithCorrectBucketKeyAndContentType() {
        adapter.upload("hello".getBytes(), "goals/abc.png", "image/png");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));

        PutObjectRequest request = captor.getValue();
        assertThat(request.bucket()).isEqualTo("media");
        assertThat(request.key()).isEqualTo("goals/abc.png");
        assertThat(request.contentType()).isEqualTo("image/png");
    }

    @Test
    void upload_shouldReturnPublicUrl_composedOfBaseBucketAndKey() {
        String url = adapter.upload("hello".getBytes(), "goals/abc.png", "image/png");

        assertThat(url).isEqualTo(PUBLIC_URL + "/media/goals/abc.png");
    }

    @Test
    void upload_shouldStripLeadingSlashFromKey() {
        String url = adapter.upload("x".getBytes(), "/goals/abc.png", "image/png");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));

        assertThat(captor.getValue().key()).isEqualTo("goals/abc.png");
        assertThat(url).isEqualTo(PUBLIC_URL + "/media/goals/abc.png");
    }

    @Test
    void upload_shouldNotProduceDoubleSlash_whenPublicUrlHasTrailingSlash() {
        ReflectionTestUtils.setField(adapter, "publicUrl", PUBLIC_URL + "/");

        String url = adapter.upload("x".getBytes(), "abc.png", "image/png");

        assertThat(url).isEqualTo(PUBLIC_URL + "/media/abc.png");
    }

    @Test
    void upload_shouldWrapS3Exception_inIllegalStateException() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("boom").build());

        assertThatThrownBy(() -> adapter.upload("x".getBytes(), "abc.png", "image/png"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se pudo subir el archivo a Supabase Storage");
    }
}
