package com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DocumentMapperTest {

  private final DocumentMapper mapper = new DocumentMapper();

  @Test
  @DisplayName("Should map domain Document to Entity correctly")
  void shouldMapDomainToEntity() {
    Document document =
        Document.builder()
            .id(UUID.randomUUID())
            .userId(new BigInteger("12345"))
            .documentName("test.pdf")
            .tags(List.of("tag1", "tag2"))
            .minioPath("path/to/file")
            .fileSize(2048L)
            .fileType("application/pdf")
            .checksum("abc123")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .status("ACTIVE")
            .build();

    DocumentEntity entity = mapper.toEntity(document);

    assertEquals(document.getId(), entity.getId());
    assertEquals(document.getUserId(), entity.getUserId());
    assertEquals(document.getDocumentName(), entity.getDocumentName());
    assertEquals(document.getMinioPath(), entity.getMinioPath());
    assertEquals(document.getFileType(), entity.getFileType());
    assertEquals(document.getFileSize(), entity.getFileSize());
    assertEquals(document.getChecksum(), entity.getChecksum());
    assertIterableEquals(document.getTags(), List.of(entity.getTags().split(",")));
  }

  @Test
  @DisplayName("Should map Entity to domain Document correctly")
  void shouldMapEntityToDomain() {
    DocumentEntity entity = new DocumentEntity();
    entity.setId(UUID.randomUUID());
    entity.setUserId(new BigInteger("456789"));
    entity.setDocumentName("entity.pdf");
    entity.setTags(String.join(",", List.of("x", "y")));
    entity.setMinioPath("minio/entity");
    entity.setFileSize(1024L);
    entity.setFileType("application/pdf");
    entity.setChecksum("xyz789");
    entity.setCreatedAt(Instant.now());
    entity.setUpdatedAt(Instant.now());

    Document document = mapper.toDomain(entity);

    assertEquals(entity.getId(), document.getId());
    assertEquals(entity.getUserId(), document.getUserId());
    assertEquals(entity.getDocumentName(), document.getDocumentName());
    assertEquals(entity.getMinioPath(), document.getMinioPath());
    assertEquals(entity.getFileType(), document.getFileType());
    assertEquals(entity.getFileSize(), document.getFileSize());
    assertEquals(entity.getChecksum(), document.getChecksum());
    assertIterableEquals(List.of(entity.getTags().split(",")), document.getTags());
  }

  @Test
  @DisplayName("Should throw NullPointerException when mapping null domain object to entity")
  void shouldThrowExceptionWhenDomainIsNull() {
    assertThrows(NullPointerException.class, () -> mapper.toEntity(null));
  }

  @Test
  @DisplayName("Should throw NullPointerException when mapping null entity to domain")
  void shouldThrowExceptionWhenEntityIsNull() {
    assertThrows(NullPointerException.class, () -> mapper.toDomain(null));
  }

  @Test
  @DisplayName("Should handle null tags gracefully")
  void shouldHandleNullTagsGracefully() {
    Document document =
        Document.builder()
            .id(UUID.randomUUID())
            .userId(new BigInteger("12345"))
            .documentName("no-tags.pdf")
            .minioPath("no-tags")
            .fileSize(123L)
            .fileType("application/pdf")
            .checksum("zzz")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .status("ACTIVE")
            .tags(null)
            .build();

    DocumentEntity entity = mapper.toEntity(document);

    assertNull(entity.getTags(), "Tags should be null when input tags are null or empty");
  }
}
