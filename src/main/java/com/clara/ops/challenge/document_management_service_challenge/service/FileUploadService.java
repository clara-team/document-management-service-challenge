package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.properties.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.exception.S3Exception;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileUploadService {

    private final MinioClient client;
    private final MinioProperties properties;


    public void uploadFile(MultipartFile file, String objectName) {
        createBucketIfNotExist();
        try (InputStream inputStream = file.getInputStream()) {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .stream(
                                    inputStream,
                                    file.getSize(),
                                    -1
                            )
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            log.error("Error while uploading file: {}", objectName, e);
            throw new S3Exception("Failed to upload file");
        }
    }

    private void createBucketIfNotExist() {
        try {
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getBucketName()).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucketName())
                        .build()
                );
            }
        } catch (Exception e) {
            log.error("Error while creating or accessing bucket: {}", properties.getBucketName(), e);
            throw new S3Exception("Failed to create or access bucket");
        }
    }

    public String getDownloadLink(String objectKey) {
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(properties.getBucketName())
                    .method(Method.GET)
                    .object(objectKey)
                            .expiry(properties.getExpiration(), TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("error obtaining url link for {}", objectKey, e);
            throw new S3Exception("Error obtaining download link");
        }
    }
}
