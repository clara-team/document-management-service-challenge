package com.clara.ops.challenge.document_management_service_challenge.infrastructure;

import com.clara.ops.challenge.document_management_service_challenge.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static io.minio.http.Method.GET;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    public void uploadFile(MultipartFile file, String filepath) {
        try {
            createBucketIfNotExists();

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filepath)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            log.info("File {} uploaded successfully!", filepath);
        } catch (Exception e) {
            log.error("Error uploading file {}!", filepath, e);
            throw new BusinessException("Error uploading file "+filepath);
        }
    }

    public String generatePresignedUrl(String filePath) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(filePath)
                        .method(GET)
                        .build()
            );
        } catch (Exception e) {
            log.error("Error generating presigned url!");
            return null;
        }
    }

    private void createBucketIfNotExists() {
        try {
            BucketExistsArgs existArgs = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();

            if (isFalse(minioClient.bucketExists(existArgs))) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Bucket {} created successfully!", bucketName);
            }
        } catch (Exception e) {
            log.error("Error creating bucket {}!", bucketName, e);
            throw new BusinessException("Error creating bucket "+bucketName);
        }
    }

}
