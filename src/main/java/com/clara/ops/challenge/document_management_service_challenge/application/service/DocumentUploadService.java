package com.clara.ops.challenge.document_management_service_challenge.application.service;

import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.storage.MinioStorageService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.input.CountingInputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DocumentUploadService {

  private static final String PDF_CONTENT_TYPE = "application/pdf";

  //Only 1 upload at a time — ensures heap usage stays within limits regardless of concurrent requests.
  private static final int MAX_CONCURRENT_UPLOADS = 1;

  // Semaphore strategy to avoid heap exhaustion: if we allowed all uploads to start simultaneously.
  private final Semaphore uploadSemaphore = new Semaphore(MAX_CONCURRENT_UPLOADS, true);

  private final MinioStorageService storageService;
  private final DocumentRepository documentRepository;

  public DocumentUploadService(
      MinioStorageService storageService, DocumentRepository documentRepository) {
    this.storageService = storageService;
    this.documentRepository = documentRepository;
  }

  /**
   * Streams the uploaded file directly from the HTTP request to MinIO without writing to disk.
   * Form fields (user, name, tags) must appear before the file part in the multipart body.
   * Concurrent uploads to MinIO are capped by a semaphore to prevent heap exhaustion.
   */
  public void upload(HttpServletRequest httpRequest) {
    try {
      JakartaServletFileUpload fileUpload = new JakartaServletFileUpload();
      FileItemInputIterator iter = fileUpload.getItemIterator(httpRequest);

      String user = null;
      String name = null;
      List<String> tags = new ArrayList<>();

      while (iter.hasNext()) {
        FileItemInput item = iter.next();
        String fieldName = item.getFieldName();

        if (item.isFormField()) {
          String value =
              new String(item.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
          String normalized =
              fieldName.endsWith("[]")
                  ? fieldName.substring(0, fieldName.length() - 2)
                  : fieldName;
          switch (normalized) {
            case "user" -> user = value;
            case "name" -> name = value;
            case "tags" -> tags.add(value);
            default -> log.warn("Ignoring unknown form field: '{}'", fieldName);
          }
        } else if ("file".equals(fieldName)) {
          validateMetadata(user, name, tags);

          String contentType =
              item.getContentType() != null ? item.getContentType() : PDF_CONTENT_TYPE;
          validateContentType(contentType);

          String objectPath = buildObjectPath(user, name);

          log.info(
              "Waiting for upload slot (available={}/{})",
              uploadSemaphore.availablePermits(),
              MAX_CONCURRENT_UPLOADS);
          uploadSemaphore.acquire();
          try (InputStream raw = item.getInputStream();
              CountingInputStream counting = new CountingInputStream(raw)) {
            log.info("Upload started: path={}, user={}", objectPath, user);
            storageService.upload(objectPath, counting, contentType);
            long fileSize = counting.getByteCount();

            // Transaction is scoped only to the DB save — not to the MinIO upload.
            // This avoids holding a DB connection open during the entire file transfer.
            saveDocument(user, name, objectPath, fileSize, contentType, tags);
            log.info("Upload complete: path={}, size={} bytes", objectPath, fileSize);
          } finally {
            uploadSemaphore.release();
            log.info(
                "Slot released (available={}/{})",
                uploadSemaphore.availablePermits(),
                MAX_CONCURRENT_UPLOADS);
          }
        }
      }
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      log.error("Upload failed — type={} message={}", e.getClass().getName(), e.getMessage(), e);
      throw new RuntimeException("Failed to process multipart upload", e);
    }
  }

  @Transactional
  public void saveDocument(
      String user,
      String name,
      String objectPath,
      long fileSize,
      String contentType,
      List<String> tags) {
    DocumentEntity entity =
        DocumentEntity.builder()
            .userName(user)
            .name(name)
            .minioPath(objectPath)
            .fileSize(fileSize)
            .fileType(contentType)
            .tags(tags)
            .build();
    documentRepository.save(entity);
  }

  private void validateMetadata(String user, String name, List<String> tags) {
    if (user == null || user.isBlank()) {
      throw new IllegalArgumentException("user must not be blank");
    }
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (tags == null || tags.isEmpty()) {
      throw new IllegalArgumentException("tags must not be empty");
    }
  }

  private void validateContentType(String contentType) {
    if (!PDF_CONTENT_TYPE.equals(contentType)) {
      throw new IllegalArgumentException("Only PDF files are accepted");
    }
  }

  private String buildObjectPath(String userName, String documentName) {
    String safeUser = userName.replaceAll("[^a-zA-Z0-9._-]", "_").replaceAll("\\.{2,}", "_");
    String safeName = documentName.replaceAll("[^a-zA-Z0-9._-]", "_").replaceAll("\\.{2,}", "_");
    return safeUser + "/" + safeName;
  }
}
