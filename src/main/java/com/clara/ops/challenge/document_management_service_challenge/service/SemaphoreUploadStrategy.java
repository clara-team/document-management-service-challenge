package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioConfig;
import com.clara.ops.challenge.document_management_service_challenge.service.dto.FileInputDTO;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@Qualifier("SemaphoreUploadStrategy") public class SemaphoreUploadStrategy implements IUploadStrategy {
  private final IMinioService minioService;
  private final MinioConfig minioConfig;
  private final Semaphore semaphore;

  public SemaphoreUploadStrategy(IMinioService minioService, MinioConfig minioConfig) {
    this.minioService = minioService;
    this.minioConfig = minioConfig;
    this.semaphore = new Semaphore(this.minioConfig.getPermitAvailable());
  }

  @Override
  public void processUpload(FileInputDTO fileInputDTO, MultipartFile multipartFile) {
    boolean bucketExists = minioService.bucketExists(minioConfig.getBucketName());
    if (!bucketExists) minioService.bucketMake(minioConfig.getBucketName());
    try (InputStream inputStream = multipartFile.getInputStream()) {
      semaphore.acquire();
      log.info(
          "Semaphore strategy: Attempting to start the thread: {}",
          Thread.currentThread().getName());
      minioService.putObject(minioConfig.getBucketName(), fileInputDTO, inputStream, -1L);
    } catch (InterruptedException e) {
      log.error(
          "Exception InterruptedException occurred in method processUpload: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Exception InterruptedException occurred in method processUpload",
          e);
    } catch (IOException e) {
      log.error("Exception IOException occurred in method processUpload: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Exception IOException occurred in method processUpload",
          e);
    } finally {
      log.info("Semaphore strategy: Ending the thread: {}", Thread.currentThread().getName());
      semaphore.release();
    }
  }
}
