package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioConfig;
import com.clara.ops.challenge.document_management_service_challenge.service.dto.FileInputDTO;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
@Qualifier("FilePartitionUploadStrategy") public class FilePartitionUploadStrategy implements IUploadStrategy {
  @Autowired private IMinioService minioService;
  @Autowired private MinioConfig minioConfig;

  @Override
  public void processUpload(FileInputDTO fileInputDTO, MultipartFile multipartFile) {
    boolean bucketExists = minioService.bucketExists(minioConfig.getBucketName());
    if (!bucketExists) minioService.bucketMake(minioConfig.getBucketName());
    try (InputStream inputStream = multipartFile.getInputStream()) {
      log.info("File partition strategy: Start the thread: {}", Thread.currentThread().getName());
      minioService.putObject(
          minioConfig.getBucketName(), fileInputDTO, inputStream, minioConfig.getPartSize());
    } catch (IOException e) {
      log.error("Exception IOException occurred in method processUpload: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Exception IOException occurred in method processUpload",
          e);
    } catch (Exception e) {
      log.error("General exception occurred in method processUpload: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "General exception occurred in method processUpload",
          e);
    } finally {
      log.info("File partition strategy: Ending the thread: {}", Thread.currentThread().getName());
    }
  }
}
