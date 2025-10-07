package com.clara.ops.challenge.document_management_service_challenge.domain.model;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.*;

/**
 * Represents a document entity containing metadata and relevant details about the document. This
 * class is designed to store and manage information related to a document within the system.
 * Attributes include a unique identifier, user information, document name, tags, storage path, URL,
 * file size, file type, timestamps for creation and updates, checksum, and status.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Document {
  UUID id;
  BigInteger userId;
  String documentName;
  List<String> tags;
  String minioPath;
  String documentUrl;
  long fileSize;
  String fileType;
  Instant updatedAt;
  Instant createdAt;
  String checksum;
  String status;
}
