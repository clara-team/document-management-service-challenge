package com.clara.ops.challenge.document_management_service_challenge.infrastructure.storage;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProps;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService {

    // 10 MB part size for multipart uploads. This allows the SDK to manage memory efficiently for large files.
    private static final long PART_SIZE = 10L * 1024 * 1024;

    private final MinioClient minioClient;
    private final MinioProps properties;

    @PostConstruct
    public void ensureBucketExists() {
        try {
            boolean exists =
                    minioClient.bucketExists(
                            BucketExistsArgs.builder().bucket(properties.getBucket()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(properties.getBucket()).build());
                log.info("Created MinIO bucket: {}", properties.getBucket());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to verify or create MinIO bucket", e);
        }
    }

    public void upload(String objectPath, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectPath)
                            // To keep heap usage low, the SDK manages multipart uploads internally and does not buffer
                            // the entire file in memory. Instead, it reads from the InputStream in chunks of PART_SIZE
                            .stream(inputStream, -1, PART_SIZE)
                            .contentType(contentType)
                            .build());
            log.info("Uploaded object to MinIO: {}", objectPath);
        } catch (Exception e) {
            throw new StorageException("Failed to upload object to MinIO: " + objectPath, e);
        }
    }

    public String generatePresignedUrl(String objectPath, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectPath)
                            .method(Method.GET)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned URL for: " + objectPath, e);
        }
    }
}
