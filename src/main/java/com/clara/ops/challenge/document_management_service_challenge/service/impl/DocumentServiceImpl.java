package com.clara.ops.challenge.document_management_service_challenge.service.impl;

import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.model.dto.*;
import com.clara.ops.challenge.document_management_service_challenge.model.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.model.entity.Tag;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.TagRepository;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import com.clara.ops.challenge.document_management_service_challenge.service.StorageService;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {

  private final DocumentRepository documentRepository;
  private final TagRepository tagRepository;
  private final StorageService storageService;

  @Autowired
  public DocumentServiceImpl(
      DocumentRepository documentRepository,
      TagRepository tagRepository,
      StorageService storageService) {
    this.documentRepository = documentRepository;
    this.tagRepository = tagRepository;
    this.storageService = storageService;
  }

  /**
   * Upload a document
   *
   * @param uploadRequest Document upload request
   * @param file The file to upload
   * @return Information about the uploaded document
   */
  @Transactional
  public DocumentInfo uploadDocument(UploadRequest uploadRequest, MultipartFile file) {
    try {
      // Upload file to MinIO
      String minioPath =
          storageService.uploadFile(
              uploadRequest.getUserId(), uploadRequest.getDocumentName(), file);

      // Create and save tags
      Set<Tag> tags = getOrCreateTags(uploadRequest.getTags());

      // Create document entity
      Document document =
          Document.builder()
              .userId(uploadRequest.getUserId())
              .documentName(uploadRequest.getDocumentName())
              .minioPath(minioPath)
              .fileSize(file.getSize())
              .fileType(file.getContentType())
              .createdAt(LocalDateTime.now())
              .build();

      // Associate tags with document
      tags.forEach(document::addTag);

      // Save document
      Document savedDocument = documentRepository.save(document);

      log.info(
          "Document uploaded successfully: id={}, user={}, name={}, size={}",
          savedDocument.getId(),
          savedDocument.getUserId(),
          savedDocument.getDocumentName(),
          savedDocument.getFileSize());

      return mapToDocumentInfo(savedDocument);
    } catch (Exception e) {
      log.error("Error uploading document: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to upload document", e);
    }
  }

  /**
   * Search for documents with optional filters
   *
   * @param searchRequest Search request containing filters
   * @return Page of document information
   */
  @Transactional(readOnly = true)
  public SearchResponse searchDocuments(SearchRequest searchRequest) {
    int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
    int size = searchRequest.getSize() != null ? searchRequest.getSize() : 10;
    Pageable pageable = PageRequest.of(page, size);

    Page<Document> documentsPage;

    String userId = searchRequest.getUserId();
    String documentName = searchRequest.getDocumentName();
    List<String> tags = searchRequest.getTags();

    // Determine which query to use based on the provided filters
    if (userId != null && documentName != null && tags != null && !tags.isEmpty()) {
      // All filters provided
      documentsPage =
          documentRepository.findByUserIdAndDocumentNameAndTagNamesExactMatch(
              userId, documentName, tags, (long) tags.size(), pageable);
    } else if (userId != null && documentName != null) {
      // User ID and document name provided
      documentsPage =
          documentRepository.findByUserIdAndDocumentNameContainingIgnoreCaseOrderByCreatedAtDesc(
              userId, documentName, pageable);
    } else if (userId != null && tags != null && !tags.isEmpty()) {
      // User ID and tags provided
      documentsPage =
          documentRepository.findByUserIdAndTagNamesExactMatch(
              userId, tags, (long) tags.size(), pageable);
    } else if (documentName != null && tags != null && !tags.isEmpty()) {
      // Document name and tags provided
      documentsPage =
          documentRepository.findByDocumentNameAndTagNamesExactMatch(
              documentName, tags, (long) tags.size(), pageable);
    } else if (userId != null) {
      // Only user ID provided
      documentsPage = documentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    } else if (documentName != null) {
      // Only document name provided
      documentsPage =
          documentRepository.findByDocumentNameContainingIgnoreCaseOrderByCreatedAtDesc(
              documentName, pageable);
    } else if (tags != null && !tags.isEmpty()) {
      // Only tags provided
      documentsPage =
          documentRepository.findByTagNamesExactMatch(tags, (long) tags.size(), pageable);
    } else {
      // No filters provided
      documentsPage = documentRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    List<DocumentInfo> documentInfoList =
        documentsPage.getContent().stream()
            .map(this::mapToDocumentInfo)
            .collect(Collectors.toList());

    return SearchResponse.builder()
        .documents(documentInfoList)
        .currentPage(documentsPage.getNumber())
        .totalItems(documentsPage.getTotalElements())
        .totalPages(documentsPage.getTotalPages())
        .build();
  }

  /**
   * Get document download URL
   *
   * @param documentId The ID of the document to download
   * @return Download URL information
   */
  @Transactional(readOnly = true)
  public DownloadResponse getDocumentDownloadUrl(Long documentId) {
    Document document =
        documentRepository
            .findById(documentId)
            .orElseThrow(
                () -> new DocumentNotFoundException("Document not found with ID: " + documentId));

    String downloadUrl = storageService.generatePresignedUrl(document.getMinioPath());

    return DownloadResponse.builder()
        .downloadUrl(downloadUrl)
        .expirationTime(System.currentTimeMillis() + (30 * 60 * 1000)) // 30 minutes expiry
        .build();
  }

  /**
   * Get or create tags
   *
   * @param tagNames List of tag names
   * @return Set of tag entities
   */
  private Set<Tag> getOrCreateTags(List<String> tagNames) {
    Set<Tag> tags = new HashSet<>();

    for (String tagName : tagNames) {
      if (StringUtils.hasText(tagName)) {
        // Find existing tag or create a new one
        Optional<Tag> existingTag = tagRepository.findByName(tagName.trim());

        if (existingTag.isPresent()) {
          tags.add(existingTag.get());
        } else {
          Tag newTag = Tag.builder().name(tagName.trim()).build();
          tags.add(newTag);
        }
      }
    }

    return tags;
  }

  /**
   * Map a Document entity to a DocumentInfo DTO
   *
   * @param document The document entity
   * @return DocumentInfo DTO
   */
  private DocumentInfo mapToDocumentInfo(Document document) {
    List<String> tagNames = new ArrayList<>();
    if (document.getTags() != null) {
      tagNames = document.getTags().stream().map(Tag::getName).collect(Collectors.toList());
    }

    return DocumentInfo.builder()
        .id(document.getId())
        .userId(document.getUserId())
        .documentName(document.getDocumentName())
        .tags(tagNames)
        .fileSize(document.getFileSize())
        .fileType(document.getFileType())
        .createdAt(document.getCreatedAt())
        .build();
  }
}
