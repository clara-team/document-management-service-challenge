package com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "documents", schema = "document_schema")
@Getter
@Setter
public class DocumentEntity {
  @Id String id;
  BigInteger userId;
  String documentName;
  @ElementCollection List<String> tags;
  String minioPath;
  long fileSize;
  String fileType;
  Instant createdAt;
  Instant updatedAt;
  long checksum;
  String status;
}
