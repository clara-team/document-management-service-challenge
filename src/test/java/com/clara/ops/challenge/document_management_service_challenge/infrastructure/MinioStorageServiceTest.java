package com.clara.ops.challenge.document_management_service_challenge.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProps;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.storage.MinioStorageService;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.storage.StorageException;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MinioStorageServiceTest {

    @Mock MinioClient minioClient;

    MinioProps properties;
    MinioStorageService service;

    @BeforeEach
    void setUp() {
        properties = new MinioProps();
        properties.setEndpoint("http://localhost:9000");
        properties.setAccessKey("key");
        properties.setSecretKey("secret");
        properties.setBucket("test-bucket");
        service = new MinioStorageService(minioClient, properties);
    }

    @Test
    void ensureBucketExists_bucketAlreadyExists_doesNotCreateBucket() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        service.ensureBucketExists();

        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, org.mockito.Mockito.never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void ensureBucketExists_bucketDoesNotExist_createsBucket() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        service.ensureBucketExists();

        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void ensureBucketExists_minioThrows_throwsIllegalStateException() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenThrow(new RuntimeException("connection refused"));

        assertThatThrownBy(() -> service.ensureBucketExists())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to verify or create MinIO bucket");
    }

    @Test
    void upload_validArgs_callsPutObject() throws Exception {
        service.upload("user/doc.pdf", InputStream.nullInputStream(), "application/pdf");

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void upload_minioThrows_throwsStorageException() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("upload failed"));

        assertThatThrownBy(
                        () ->
                                service.upload(
                                        "user/doc.pdf",
                                        InputStream.nullInputStream(),
                                        "application/pdf"))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("user/doc.pdf");
    }

    @Test
    void generatePresignedUrl_validArgs_returnsUrl() throws Exception {
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://minio/doc.pdf?signature=abc");

        String url = service.generatePresignedUrl("user/doc.pdf", 15);

        assertThat(url).contains("signature=abc");
    }

    @Test
    void generatePresignedUrl_minioThrows_throwsStorageException() throws Exception {
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("presign failed"));

        assertThatThrownBy(() -> service.generatePresignedUrl("user/doc.pdf", 15))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("user/doc.pdf");
    }
}
