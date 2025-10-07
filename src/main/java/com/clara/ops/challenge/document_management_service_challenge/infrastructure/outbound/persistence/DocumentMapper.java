package com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * The DocumentMapper is a utility class responsible for mapping between the domain model Document
 * and the persistence entity DocumentEntity.
 *
 * <p>This class facilitates the conversion of data back and forth between these two representations
 * and ensures that the internal state and logic required during the transformation processes, such
 * as handling list of tags, are consistently managed.
 *
 * <p>The class employs the Lombok @Slf4j annotation for logging relevant debugging information
 * during transformation operations.
 */
@Slf4j
@Component
public class DocumentMapper {

  public DocumentEntity toEntity(Document document) {
    log.debug("Mapping document to entity: {}", document);

    if (document == null) {
      log.error("Document cannot be null");
      throw new NullPointerException("Document cannot be null");
    }

    DocumentEntity entity = new DocumentEntity();
    entity.setId(document.getId());
    entity.setUserId(document.getUserId());
    entity.setDocumentName(document.getDocumentName());
    entity.setTags(
        document.getTags() != null && !document.getTags().isEmpty()
            ? document.getTags().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.joining(","))
            : null);
    entity.setMinioPath(document.getMinioPath());
    entity.setFileSize(document.getFileSize());
    entity.setFileType(document.getFileType());
    entity.setDocumentUrl(document.getDocumentUrl());
    entity.setChecksum(document.getChecksum());
    entity.setStatus(document.getStatus());
    entity.setCreatedAt(document.getCreatedAt());
    entity.setUpdatedAt(document.getUpdatedAt());
    return entity;
  }

  public Document toDomain(DocumentEntity entity) {

    log.debug("Mapping entity to document: {}", entity);
    return Document.builder()
        .id(entity.getId())
        .userId(entity.getUserId())
        .documentName(entity.getDocumentName())
        .tags(parseTags(entity.getTags()))
        .minioPath(entity.getMinioPath())
        .fileSize(entity.getFileSize())
        .fileType(entity.getFileType())
        .documentUrl(entity.getDocumentUrl())
        .checksum(entity.getChecksum())
        .status(entity.getStatus())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  private List<String> parseTags(String tagsString) {
    if (tagsString == null || tagsString.isBlank()) {
      return Collections.emptyList();
    }
    return Arrays.stream(tagsString.split(","))
        .map(String::trim)
        .filter(tag -> !tag.isEmpty())
        .collect(Collectors.toList());
  }
}
