package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.domain.dto.DocumentDto;
import com.clara.ops.challenge.document_management_service_challenge.domain.dto.DownloadUrlDto;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.exceptions.DataNotFound;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.UserRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.specs.DocumentSpecifications;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentService {
  private final DocumentRepository documentRepository;
  private final UserRepository userRepository;
  private final MinioClient minioClient;
  private final Semaphore semaphore = new Semaphore(10);

  @Value("${minio.bucket-name:document-bucket}")
  private String bucketName;

  /**
   * Get documents
   *
   * @param username
   * @param documentName
   * @param tags
   * @param page
   * @param size
   * @return
   */
  public Page<DocumentDto> getDocuments(
      String username, String documentName, String tags, Integer page, Integer size) {
    log.info(
        "Getting documents for user: {}, documentName: {}, tags: {}, page: {}, size: {}",
        username,
        documentName,
        tags,
        page,
        size);

    var userFromDb = userRepository.findByUsername(username);
    var user = (userFromDb.isPresent()) ? userFromDb.get() : null;

    var pageable = PageRequest.of(page - 1, size);

    var spec =
        Specification.where(DocumentSpecifications.userEquals(user))
            .and(DocumentSpecifications.nameEquals(documentName))
            .and(DocumentSpecifications.tagsContains(getTagsList(tags)));

    var documentPage = documentRepository.findAll(spec, pageable);
    var documentDtos = documentPage.getContent().stream().map(this::mapToDto).toList();

    return new PageImpl<>(documentDtos, pageable, documentPage.getTotalElements());
  }

  /**
   * Generate a download URL for a document.
   *
   * @param documentId The ID of the document to generate the download URL for.
   * @return The download URL for the document.
   * @throws MinioException If there is an error generating the URL.
   * @throws IOException If there is an error generating the URL.
   * @throws NoSuchAlgorithmException If there is an error generating the URL.
   * @throws InvalidKeyException If there is an error generating the URL.
   */
  public DownloadUrlDto generateDownloadUrl(Long documentId)
      throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
    var document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new DataNotFound("Document not found"));

    var objectName = document.getPath();

    var expirationTime = 3600;

    return new DownloadUrlDto(
        minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .method(Method.GET) // Specify the HTTP method
                .expiry(expirationTime)
                .build()));
  }

  /**
   * uploadFile method
   *
   * @param file
   * @param metadata
   */
  public void uploadFile(MultipartFile file, Map<String, String> metadata)
      throws InterruptedException,
          ServerException,
          InsufficientDataException,
          ErrorResponseException,
          IOException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          InvalidResponseException,
          XmlParserException,
          InternalException {
    log.info("Uploading file with metadata: {}", metadata);

    log.info("Thread {} attempting to acquire a permit", Thread.currentThread().getId());

    semaphore.acquire();

    try {
      log.info("Thread {} acquired a permit", Thread.currentThread().getId());
      processUploadedFile(file, metadata);
    } catch (Exception e) {
      log.error("Error uploading file: {}", e.getMessage(), e);
      throw e;
    } finally {
      log.info("Thread {} releasing the permit", Thread.currentThread().getId());
      semaphore.release();
    }
  }

  /**
   * Process the uploaded file
   *
   * @param file
   * @param metadata
   * @throws ServerException
   * @throws InsufficientDataException
   * @throws ErrorResponseException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws InvalidResponseException
   * @throws XmlParserException
   * @throws InternalException
   */
  private void processUploadedFile(MultipartFile file, Map<String, String> metadata)
      throws DataNotFound,
          ServerException,
          InsufficientDataException,
          ErrorResponseException,
          IOException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          InvalidResponseException,
          XmlParserException,
          InternalException {

    String username = metadata.get("username");

    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }

    var userFromDb = userRepository.findByUsername(username);

    if (userFromDb.isEmpty()) {
      throw new DataNotFound("User not found");
    }

    var user = userFromDb.get();

    var path = this.uploadFileToBucket(file, user.getUsername());

    this.saveDocument(
        user,
        file.getOriginalFilename(),
        file.getContentType(),
        file.getSize(),
        getTagsList(metadata.get("tags")),
        path);
  }

  /**
   * @param user
   * @param name
   * @param type
   * @param size
   * @param tags
   * @param path
   */
  private void saveDocument(
      User user, String name, String type, Long size, List<String> tags, String path) {
    var document =
        Document.builder()
            .name(name)
            .type(type)
            .size(size)
            .tags(tags)
            .path(path)
            .user(user)
            .createdAt(LocalDateTime.now())
            .build();

    documentRepository.save(document);
  }

  /**
   * Upload file to Minio
   *
   * @param file
   * @param userPath
   * @return
   */
  private String uploadFileToBucket(MultipartFile file, String userPath)
      throws IOException,
          ServerException,
          InsufficientDataException,
          ErrorResponseException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          InvalidResponseException,
          XmlParserException,
          InternalException {

    var originalFilename = file.getOriginalFilename();
    if (originalFilename == null) {
      throw new IllegalArgumentException("File name cannot be null");
    }

    var objectKey = String.format("%s/%s", userPath, originalFilename);

    try (var inputStream = file.getInputStream()) {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucketName).object(objectKey).stream(
                  inputStream, file.getSize(), -1)
              .contentType(file.getContentType())
              .build());

      log.info("File uploaded successfully: {}", objectKey);
      return objectKey;
    } catch (Exception e) {
      log.error("Error uploading file: {}", e.getMessage(), e);
      throw e;
    }
  }

  /**
   * @param document
   * @return
   */
  private DocumentDto mapToDto(Document document) {
    return new DocumentDto(
        document.getId(),
        document.getName(),
        document.getTags(),
        document.getType(),
        document.getSize(),
        document.getPath(),
        document.getCreatedAt().toString());
  }

  /**
   * @param tags
   * @return
   */
  private List<String> getTagsList(String tags) {
    String processedTags = (tags == null) ? "" : tags;

    // Split the tags string by commas, trim whitespace, and filter out blank entries
    return Arrays.stream(processedTags.split(","))
        .map(String::trim)
        .filter(tag -> !tag.isBlank())
        .toList();
  }
}
