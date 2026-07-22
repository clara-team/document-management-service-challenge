package com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence;

import jakarta.persistence.*;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/**
 * The DocumentEntity class represents a database entity used for storing and retrieving document
 * information. It is annotated as a JPA entity and maps to the "documents" table in the
 * "document_schema" schema.
 *
 * <p>This class provides all the necessary fields to represent a document's metadata, including
 * identifiers, storage paths, user and document information, as well as timestamps for record
 * tracking.
 *
 * <p>Features include: - Automatic generation of getters, setters, and constructors using Lombok. -
 * Integration with JPA for database interactions. - Encapsulation of details such as document
 * storage location, checksum, and status for persistence contexts.
 *
 * <p>Relationships: - Intended to operate with repository interfaces like {@link
 * DocumentRepository}. - Used for mapping between persistence models and domain models via mappers
 * like {@link DocumentMapper}.
 */
@Entity
@Table(name = "documents", schema = "document_schema")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class DocumentEntity {
  @Id
  @Column(columnDefinition = "uuid", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private BigInteger userId;

  @Column(name = "document_name", nullable = false)
  private String documentName;

  @Column(name = "tags")
  private String tags;

  @Column(name = "minio_path", length = 512)
  private String minioPath;

  @Column(name = "document_url", length = 512)
  private String documentUrl;

  @Column(name = "file_size")
  private long fileSize;

  @Column(name = "file_type", length = 50)
  private String fileType;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "checksum")
  private String checksum;

  @Column(name = "status", length = 50)
  private String status;
}
